// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.liteloader;

import com.ddjyx.neohmcl.download.ComponentRemoteVersionList;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.download.ComponentRemoteVersion;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.game.Library;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

@NotNullByDefault
public final class LiteLoaderRemoteVersion extends ComponentRemoteVersion {

    public static Task<ComponentRemoteVersionList<LiteLoaderRemoteVersion>> fetchAsync(GameVersionNumber gameVersion) {
        @JsonSerializable
        record LiteLoaderRemoteVersionRecord(
                String gameVersion,
                String version,
                boolean snapshot,
                String url,
                String tweakClass,
                List<Library> libraries
        ) {
        }

        return Task.supplyAsync(() -> {
            List<LiteLoaderRemoteVersionRecord> records;

            try (var input = LiteLoaderRemoteVersion.class.getResourceAsStream("/assets/liteloader/versions.json")) {
                if (input == null) {
                    LOG.warning("Failed to load LiteLoader versions.json from resources");
                    return ComponentRemoteVersionList.of(GameComponentType.LITELOADER);
                }

                records = JsonUtils.GSON.fromJson(
                        new String(input.readAllBytes(), StandardCharsets.UTF_8),
                        JsonUtils.listTypeOf(LiteLoaderRemoteVersionRecord.class));
            }

            var versions = new TreeSet<LiteLoaderRemoteVersion>();
            for (var record : records) {
                if (GameVersionNumber.asGameVersion(record.gameVersion).equals(gameVersion)) {
                    versions.add(new LiteLoaderRemoteVersion(
                            gameVersion,
                            record.version,
                            record.snapshot ? Type.SNAPSHOT : Type.RELEASE,
                            List.of(record.url),
                            record.tweakClass,
                            List.copyOf(record.libraries)
                    ));
                }
            }
            return ComponentRemoteVersionList.of(GameComponentType.LITELOADER, versions);
        });
    }

    private final String tweakClass;
    private final Collection<Library> libraries;

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param urls        the installer or universal jar original URL.
     */
    LiteLoaderRemoteVersion(GameVersionNumber gameVersion, String selfVersion, Type type, List<String> urls, String tweakClass, Collection<Library> libraries) {
        super(GameComponentType.LITELOADER, gameVersion, selfVersion, null, type, urls);

        this.tweakClass = tweakClass;
        this.libraries = libraries;
    }

    public Collection<Library> getLibraries() {
        return libraries;
    }

    public String getTweakClass() {
        return tweakClass;
    }

    @Override
    public Task<GameInstancePatch> getInstallTask(DefaultDependencyManager dependencyManager, GameInstanceManifest baseManifest, Path modsDirectory) {
        return new LiteLoaderInstallTask(dependencyManager, baseManifest, this);
    }
}
