// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.cleanroom;

import com.ddjyx.neohmcl.download.*;
import com.ddjyx.neohmcl.download.forge.ForgeNewInstallProfile;
import com.ddjyx.neohmcl.download.forge.ForgeNewInstallTask;
import com.ddjyx.neohmcl.download.game.GameDownloadTask;
import com.ddjyx.neohmcl.game.GameComponentAnalyzer;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.task.FileDownloadTask;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.CompressingUtils;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.ddjyx.neohmcl.download.UnsupportedInstallationException.CLEANROOM_NOT_COMPATIBLE_WITH_FORGE;

public final class CleanroomInstallTask extends Task<GameInstancePatch> {

    private final DefaultDependencyManager dependencyManager;
    private final GameInstanceManifest manifest;
    /// Minecraft version whose vanilla client JAR is required by the installer processors.
    private final GameVersionNumber gameVersion;
    private final @Nullable CleanroomRemoteVersion remote;
    private @Nullable Path installer;
    private @Nullable FileDownloadTask dependent;
    private @Nullable Task<GameInstancePatch> task;
    private @Nullable String selfVersion;

    /// Creates a Cleanroom task that downloads the selected installer.
    ///
    /// @param dependencyManager repository-scoped download services
    /// @param manifest          working manifest receiving the Cleanroom patch
    /// @param remoteVersion     selected Cleanroom version
    public CleanroomInstallTask(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest,
            CleanroomRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;
        this.gameVersion = remoteVersion.getGameVersion();
        this.remote = remoteVersion;

        setSignificance(TaskSignificance.MODERATE);
    }

    /// Creates a Cleanroom task backed by an existing local installer.
    ///
    /// @param dependencyManager repository-scoped download services
    /// @param manifest          working manifest receiving the Cleanroom patch
    /// @param gameVersion       Minecraft version expected by the installation
    /// @param selfVersion       Cleanroom version recorded in the returned patch
    /// @param installer         Cleanroom installer JAR
    public CleanroomInstallTask(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest,
            GameVersionNumber gameVersion,
            String selfVersion,
            Path installer) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;
        this.gameVersion = gameVersion;
        this.selfVersion = selfVersion;
        this.remote = null;
        this.installer = installer;

        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void preExecute() throws Exception {
        if (installer == null) {
            installer = Files.createTempFile("cleanroom-installer", ".jar");

            CleanroomRemoteVersion remoteVersion = Objects.requireNonNull(remote);
            DownloadProvider downloadProvider = dependencyManager.getDownloadProvider();
            dependent = new FileDownloadTask(
                    downloadProvider.getDownloadCandidates(remoteVersion),
                    installer, null);
            dependent.setCacheRepository(dependencyManager.getCacheRepository());
            dependent.setCaching(true);
            dependent.addIntegrityCheckHandler(FileDownloadTask.ZIP_INTEGRITY_CHECK_HANDLER);
        }
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        if (remote != null) {
            Files.deleteIfExists(Objects.requireNonNull(installer));
        }

        setResult(Objects.requireNonNull(task).getResult());
    }

    @Override
    public Collection<Task<?>> getDependents() {
        return dependent == null ? Collections.emptySet() : Collections.singleton(dependent);
    }

    @Override
    public Collection<Task<?>> getDependencies() {
        return Collections.singleton(Objects.requireNonNull(task));
    }

    @Override
    public void execute() throws IOException, VersionMismatchException, UnsupportedInstallationException {
        String cleanroomVersion;
        if (selfVersion == null) {
            cleanroomVersion = Objects.requireNonNull(remote).getSelfVersion();
        } else {
            cleanroomVersion = selfVersion;
        }

        task = new GameDownloadTask(dependencyManager, manifest)
                .thenComposeAsync(minecraftJar -> new ForgeNewInstallTask(
                        dependencyManager,
                        manifest,
                        minecraftJar,
                        cleanroomVersion,
                        Objects.requireNonNull(installer)))
                .thenApplyAsync(patch -> patch.withId(GameComponentType.CLEANROOM));
    }

    /// Creates a task that installs Cleanroom from a local installer JAR.
    ///
    /// @param dependencyManager repository-scoped download services
    /// @param manifest          working manifest receiving the Cleanroom patch
    /// @param gameVersion       Minecraft version expected by the installation
    /// @param installer         Cleanroom installer JAR
    /// @return the task producing the Cleanroom patch
    /// @throws IOException              if the installer profile is missing, malformed, or not a
    ///                                  Cleanroom profile
    /// @throws VersionMismatchException if the installer targets another Minecraft version
    /// @throws UnsupportedInstallationException if the manifest already contains Forge
    public static Task<GameInstancePatch> install(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest,
            String gameVersion,
            Path installer) throws IOException, VersionMismatchException, UnsupportedInstallationException {
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(installer)) {
            String installProfileText = Files.readString(fs.getPath("install_profile.json"));
            Map<?, ?> installProfile = JsonUtils.fromNonNullJson(installProfileText, Map.class);
            if (GameComponentType.CLEANROOM.getPatchId().equals(installProfile.get("profile"))) {
                GameComponentAnalyzer analyzer = GameComponentAnalyzer.analyze(manifest, GameVersionNumber.asGameVersion(gameVersion));
                if (analyzer.has(GameComponentType.FORGE)) {
                    throw new UnsupportedInstallationException(CLEANROOM_NOT_COMPATIBLE_WITH_FORGE);
                }

                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion);
                return new CleanroomInstallTask(
                        dependencyManager,
                        manifest,
                        GameVersionNumber.asGameVersion(gameVersion),
                        modifyVersion(profile.getVersion()),
                        installer);
            } else {
                throw new IOException();
            }
        }
    }

    private static String modifyVersion(String version) {
        return version.replace("cleanroom-", "");
    }
}
