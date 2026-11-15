// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.server;

import com.google.gson.JsonParseException;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceID;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.ModpackConfiguration;
import com.ddjyx.neohmcl.modpack.ModpackManifest;
import com.ddjyx.neohmcl.modpack.ModpackProvider;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.TolerableValidationException;
import com.ddjyx.neohmcl.util.gson.Validation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ServerModpackManifest implements ModpackManifest, Validation {
    private final String name;
    private final String author;
    private final String version;
    private final String description;
    private final String fileApi;
    private final List<ModpackConfiguration.FileInformation> files;
    private final List<Addon> addons;

    public ServerModpackManifest() {
        this("", "", "", "", "", Collections.emptyList(), Collections.emptyList());
    }

    public ServerModpackManifest(String name, String author, String version, String description, String fileApi, List<ModpackConfiguration.FileInformation> files, List<Addon> addons) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.description = description;
        this.fileApi = fileApi;
        this.files = files;
        this.addons = addons;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getFileApi() {
        return fileApi;
    }

    public List<ModpackConfiguration.FileInformation> getFiles() {
        return files;
    }

    public List<Addon> getAddons() {
        return addons;
    }

    @Override
    public ModpackProvider getProvider() {
        return ServerModpackProvider.INSTANCE;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        if (fileApi == null)
            throw new JsonParseException("ServerModpackManifest.fileApi cannot be blank");
        if (files == null)
            throw new JsonParseException("ServerModpackManifest.files cannot be null");
    }

    public static final class Addon {
        private final String id;
        private final String version;

        public Addon() {
            this("", "");
        }

        public Addon(String id, String version) {
            this.id = id;
            this.version = version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }
    }

    public Modpack toModpack() throws IOException {
        String gameVersion = addons.stream().filter(x -> GameComponentType.GAME.getPatchId().equals(x.id)).findAny()
                .orElseThrow(() -> new IOException("Cannot find game version")).getVersion();
        return new Modpack(name, author, version, gameVersion, description, this) {
            @Override
            public Task<?> getInstallTask(
                    DefaultDependencyManager dependencyManager,
                    Path zipFile,
                    GameInstanceID instanceId,
                    String iconUrl,
                    @Nullable Set<String> excludedFiles) {
                return new ServerModpackLocalInstallTask(dependencyManager, zipFile, this, ServerModpackManifest.this, instanceId);
            }
        };
    }

}
