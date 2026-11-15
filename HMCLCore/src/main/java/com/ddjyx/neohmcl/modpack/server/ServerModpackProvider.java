// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.server;

import com.google.gson.JsonParseException;
import kala.compress.archivers.zip.ZipArchiveReader;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.game.DefaultGameInstance;
import com.ddjyx.neohmcl.modpack.MismatchedModpackTypeException;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.ModpackProvider;
import com.ddjyx.neohmcl.modpack.ModpackUpdateTask;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.CompressingUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public final class ServerModpackProvider implements ModpackProvider {
    public static final ServerModpackProvider INSTANCE = new ServerModpackProvider();

    @Override
    public String getName() {
        return "Server";
    }

    @Override
    public Task<?> createCompletionTask(DefaultDependencyManager dependencyManager, DefaultGameInstance instance) {
        return new ServerModpackCompletionTask(dependencyManager, instance);
    }

    @Override
    public Task<?> createUpdateTask(
            DefaultDependencyManager dependencyManager,
            DefaultGameInstance instance,
            Path zipFile,
            Modpack modpack,
            @Nullable Set<String> excludedFiles) throws MismatchedModpackTypeException {
        if (!(modpack.getManifest() instanceof ServerModpackManifest serverModpackManifest))
            throw new MismatchedModpackTypeException(getName(), modpack.getManifest().getProvider().getName());

        return new ModpackUpdateTask(instance, new ServerModpackLocalInstallTask(dependencyManager, zipFile, modpack, serverModpackManifest, instance));
    }

    @Override
    public Modpack readManifest(ZipArchiveReader zip, Path file) throws IOException, JsonParseException {
        String json = CompressingUtils.readTextZipEntry(zip, "server-manifest.json");
        ServerModpackManifest manifest = JsonUtils.fromNonNullJson(json, ServerModpackManifest.class);
        return manifest.toModpack();
    }
}
