// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.legacyfabric;

import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.task.FileDownloadTask;
import com.ddjyx.neohmcl.task.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class LegacyFabricAPIInstallTask extends Task<GameInstancePatch> {

    private final DefaultDependencyManager dependencyManager;
    private final GameInstanceManifest manifest;
    private final LegacyFabricAPIRemoteVersion remote;
    private final Path modsDirectory;
    private final List<Task<?>> dependencies = new ArrayList<>(1);

    /// @param dependencyManager the dependency manager
    /// @param manifest           the manifest being installed into
    /// @param remoteVersion      the Legacy Fabric API remote version
    /// @param modsDirectory      the target mods directory (must already be resolved by the caller)
    public LegacyFabricAPIInstallTask(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest,
            LegacyFabricAPIRemoteVersion remoteVersion,
            Path modsDirectory) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;
        this.remote = remoteVersion;
        this.modsDirectory = modsDirectory;
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean isRelyingOnDependencies() {
        return false;
    }

    @Override
    public void execute() throws IOException {
        dependencies.add(new FileDownloadTask(
                remote.getVersion().file().url(),
                modsDirectory.resolve("legacy-fabric-api-" + remote.getVersion().version() + ".jar"),
                remote.getVersion().file().getIntegrityCheck())
        );
    }
}
