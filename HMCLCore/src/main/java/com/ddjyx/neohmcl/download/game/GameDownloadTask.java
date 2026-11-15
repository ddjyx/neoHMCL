// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.game;

import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.download.DownloadProvider;
import com.ddjyx.neohmcl.game.DownloadInfo;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.task.CacheFileTask;
import com.ddjyx.neohmcl.task.Task;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/// Obtains a Minecraft client JAR from content-addressed cache storage.
@NotNullByDefault
public final class GameDownloadTask extends Task<Path> {

    /// The dependency manager supplying downloads and cache access.
    private final DefaultDependencyManager dependencyManager;

    /// The resolved manifest that supplies client download metadata.
    private final GameInstanceManifest manifest;

    /// The cache task created during execution.
    private final List<Task<?>> dependencies = new ArrayList<>();

    /// Creates a task that returns a cached Minecraft client JAR.
    ///
    /// @param dependencyManager the dependency manager used for resolution and downloading
    /// @param manifest          the manifest supplying client download metadata
    public GameDownloadTask(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;

        setSignificance(TaskSignificance.MODERATE);
    }

    /// Returns the cache operation created by [#execute()], if execution has started.
    ///
    /// @return the live dependency collection
    @Override
    public Collection<Task<?>> getDependencies() {
        return dependencies;
    }

    /// Creates the checksum-aware cache download.
    @Override
    public void execute() {
        DownloadInfo downloadInfo = manifest.getDownloadInfo();
        @Nullable String sha1 = downloadInfo.getSha1();
        CacheFileTask cacheTask;
        if (sha1 != null) {
            cacheTask = new CacheFileTask(
                    dependencyManager.getDownloadProvider()
                            .getDownloadCandidates(downloadInfo.getUrl()),
                    sha1);
        } else {
            DownloadProvider downloadProvider = dependencyManager.getDownloadProvider();
            cacheTask = new CacheFileTask(
                    downloadProvider.getDownloadCandidates(downloadInfo.getUrl()));
        }
        cacheTask.setCacheRepository(dependencyManager.getCacheRepository());
        cacheTask.storeTo(this::setResult);
        dependencies.add(cacheTask);
    }

    /// Requests post-execution so the completed destination can be returned.
    @Override
    public boolean doPostExecute() {
        return true;
    }

    /// Returns the downloaded or previously validated client JAR.
    ///
    /// @throws IOException if no regular cached JAR is available
    @Override
    public void postExecute() throws IOException {
        @Nullable Path result = getResult();
        //noinspection ConstantValue
        if (result == null || !Files.isRegularFile(result)) {
            throw new IOException("Minecraft client JAR was not downloaded");
        }
    }
}
