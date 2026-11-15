// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.server;

import com.ddjyx.neohmcl.game.DefaultGameInstance;
import com.ddjyx.neohmcl.game.GameComponentAnalyzer;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.modpack.ModAdviser;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.ModpackConfiguration;
import com.ddjyx.neohmcl.modpack.ModpackExportInfo;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.DigestUtils;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.Zipper;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

/// Exports one registered game instance as an HMCL server modpack archive.
@NotNullByDefault
public class ServerModpackExportTask extends Task<Void> {
    /// The fixed instance snapshot exported by this task.
    private final DefaultGameInstance instance;

    /// The validated export configuration.
    private final ModpackExportInfo exportInfo;

    /// The archive written by this task.
    private final Path modpackFile;

    /// Creates a server modpack export task.
    ///
    /// @param instance    the registered instance snapshot to export
    /// @param exportInfo  the export configuration
    /// @param modpackFile the archive to write
    public ServerModpackExportTask(DefaultGameInstance instance, ModpackExportInfo exportInfo, Path modpackFile) {
        this.instance = instance;
        this.exportInfo = exportInfo.validate();
        this.modpackFile = modpackFile;

        onDone().register(event -> {
            if (event.isFailed()) {
                try {
                    Files.deleteIfExists(modpackFile);
                } catch (IOException e) {
                    LOG.warning("Failed to delete modpack file: " + modpackFile, e);
                }
            }
        });
    }

    /// {@inheritDoc}
    @Override
    public void execute() throws Exception {
        var instanceId = instance.getId();
        ArrayList<String> blackList = new ArrayList<>(ModAdviser.MODPACK_BLACK_LIST);
        blackList.add(instanceId + ".jar");
        blackList.add(instanceId + ".json");
        LOG.info("Compressing game files without some files in blacklist, including files or directories: usernamecache.json, asm, logs, backups, versions, assets, usercache.json, libraries, crash-reports, launcher_profiles.json, NVIDIA, TCNodeTracker");
        try (Zipper zip = new Zipper(modpackFile)) {
            Path runDirectory = instance.getRunDirectory();
            List<ModpackConfiguration.FileInformation> files = new ArrayList<>();
            zip.putDirectoryProgressed(runDirectory, "overrides", path -> {
                if (Modpack.acceptFile(path, blackList, exportInfo.getWhitelist())) {
                    Path file = runDirectory.resolve(path);
                    if (Files.isRegularFile(file)) {
                        String relativePath = runDirectory.relativize(file).normalize().toString().replace(File.separatorChar, '/');
                        files.add(new ModpackConfiguration.FileInformation(relativePath, DigestUtils.digestToString("SHA-1", file)));
                    }
                    return true;
                } else {
                    return false;
                }
            }, this::updateProgress);

            GameVersionNumber version = instance.getVersion();
            if (version == GameVersionNumber.unknown()) {
                throw new IOException("Cannot parse the version of " + instanceId);
            }
            String gameVersion = version.toString();
            List<ServerModpackManifest.Addon> addons = new ArrayList<>();
            addons.add(new ServerModpackManifest.Addon(GameComponentType.GAME.getPatchId(), gameVersion));

            for (GameComponentAnalyzer.Mark mark : instance.getAnalyzer()) {
                if ((mark.componentType().isModLoader() || mark.componentType() == GameComponentType.OPTIFINE)
                        && mark.version() != null) {
                    addons.add(new ServerModpackManifest.Addon(mark.componentType().getPatchId(), mark.version()));
                }
            }

            ServerModpackManifest manifest = new ServerModpackManifest(exportInfo.getName(), exportInfo.getAuthor(), exportInfo.getVersion(), exportInfo.getDescription(), StringUtils.removeSuffix(exportInfo.getFileApi(), "/"), files, addons);
            zip.putTextFile(JsonUtils.GSON.toJson(manifest), "server-manifest.json");
        }
    }

    /// Export options supported by the server modpack format.
    public static final ModpackExportInfo.Options OPTION = new ModpackExportInfo.Options()
            .requireAuthor()
            .requireFileApi(false);
}
