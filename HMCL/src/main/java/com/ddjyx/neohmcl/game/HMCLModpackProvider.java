// SPDX-FileCopyrightText: Copyright (C) 2022 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.game;

import com.google.gson.JsonParseException;
import kala.compress.archivers.zip.ZipArchiveReader;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.modpack.MismatchedModpackTypeException;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.ModpackProvider;
import com.ddjyx.neohmcl.modpack.ModpackUpdateTask;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.CompressingUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public final class HMCLModpackProvider implements ModpackProvider {
    public static final HMCLModpackProvider INSTANCE = new HMCLModpackProvider();

    @Override
    public String getName() {
        return "HMCL";
    }

    @Override
    public @Nullable Task<?> createCompletionTask(DefaultDependencyManager dependencyManager, DefaultGameInstance instance) {
        return null;
    }

    @Override
    public Task<?> createUpdateTask(
            DefaultDependencyManager dependencyManager,
            DefaultGameInstance instance,
            Path zipFile,
            Modpack modpack,
            @Nullable Set<String> excludedFiles) throws MismatchedModpackTypeException {
        if (!(modpack.getManifest() instanceof HMCLModpackManifest))
            throw new MismatchedModpackTypeException(getName(), modpack.getManifest().getProvider().getName());

        if (!(dependencyManager.getGameRepository() instanceof HMCLGameRepository repository)) {
            throw new IllegalArgumentException("HMCLModpackProvider requires HMCLGameRepository");
        }

        return new ModpackUpdateTask(instance, new HMCLModpackInstallTask(repository, zipFile, modpack, instance));
    }

    @Override
    public Modpack readManifest(ZipArchiveReader file, Path path) throws IOException, JsonParseException {
        String manifestJson = CompressingUtils.readTextZipEntry(file, "modpack.json");
        Modpack manifest = JsonUtils.fromNonNullJson(manifestJson, HMCLModpack.class);
        String gameJson = CompressingUtils.readTextZipEntry(file, "minecraft/pack.json");
        GameInstanceManifest game = JsonUtils.fromNonNullJson(gameJson, GameInstanceManifest.class);
        if (game.jar() == null)
            if (StringUtils.isBlank(manifest.getVersion()))
                throw new JsonParseException("Cannot recognize the game version of modpack " + file + ".");
            else
                manifest.setManifest(HMCLModpackManifest.INSTANCE);
        else
            manifest.setManifest(HMCLModpackManifest.INSTANCE).setGameVersion(game.jar().id());
        return manifest;
    }

    private final static class HMCLModpack extends Modpack {
        @Override
        public Task<?> getInstallTask(
                DefaultDependencyManager dependencyManager,
                Path zipFile,
                GameInstanceID instanceId,
                String iconUrl,
                @Nullable Set<String> excludedFiles) {
            return new HMCLModpackInstallTask((HMCLGameRepository) dependencyManager.getGameRepository(), zipFile, this, instanceId);
        }
    }

}
