// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.forge;

import com.ddjyx.neohmcl.download.ArtifactMalformedException;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.game.GameRepository;
import com.ddjyx.neohmcl.game.Library;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ForgeOldInstallTask extends Task<GameInstancePatch> {

    private final DefaultDependencyManager dependencyManager;
    private final GameInstanceManifest manifest;
    private final Path installer;
    private final String selfVersion;
    private final List<Task<?>> dependencies = new ArrayList<>(1);

    ForgeOldInstallTask(DefaultDependencyManager dependencyManager, GameInstanceManifest manifest, String selfVersion, Path installer) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;
        this.installer = installer;
        this.selfVersion = selfVersion;

        setSignificance(TaskSignificance.MAJOR);
    }

    @Override
    public List<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        try (ZipFile zipFile = new ZipFile(installer.toFile())) {
            InputStream stream = zipFile.getInputStream(zipFile.getEntry("install_profile.json"));
            if (stream == null)
                throw new ArtifactMalformedException("Malformed forge installer file, install_profile.json does not exist.");
            ForgeInstallProfile installProfile = JsonUtils.fromNonNullJsonFully(stream, ForgeInstallProfile.class);

            // unpack the universal jar in the installer file.
            Library forgeLibrary = new Library(installProfile.install().getPath());
            GameRepository gameRepository = dependencyManager.getGameRepository();
            Path forgeFile = gameRepository.getLayout().getLibraryFile(manifest.id(), forgeLibrary);
            Files.createDirectories(forgeFile.getParent());

            ZipEntry forgeEntry = zipFile.getEntry(installProfile.install().getFilePath());
            try (InputStream is = zipFile.getInputStream(forgeEntry);
                 OutputStream os = Files.newOutputStream(forgeFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                is.transferTo(os);
            }

            setResult(GameInstancePatch.fromManifest(
                    installProfile.versionInfo(),
                    GameComponentType.FORGE.getPatchId(),
                    selfVersion,
                    GameInstancePatch.PRIORITY_LOADER));
            dependencies.add(dependencyManager.checkComponentCompletionAsync(installProfile.versionInfo(), true));
        } catch (ZipException ex) {
            throw new ArtifactMalformedException("Malformed forge installer file", ex);
        }
    }
}
