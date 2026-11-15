// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.neoforge;

import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.download.DownloadProvider;
import com.ddjyx.neohmcl.download.VersionMismatchException;
import com.ddjyx.neohmcl.download.forge.*;
import com.ddjyx.neohmcl.download.game.GameDownloadTask;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.task.FileDownloadTask;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.CompressingUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.ddjyx.neohmcl.util.StringUtils.removePrefix;
import static com.ddjyx.neohmcl.util.StringUtils.removeSuffix;

public final class NeoForgeInstallTask extends Task<GameInstancePatch> {
    private final DefaultDependencyManager dependencyManager;

    private final GameInstanceManifest manifest;

    private final NeoForgeRemoteVersion remoteVersion;

    private Path installer = null;

    private FileDownloadTask dependent;

    private Task<GameInstancePatch> dependency;

    public NeoForgeInstallTask(DefaultDependencyManager dependencyManager, GameInstanceManifest manifest, NeoForgeRemoteVersion remoteVersion) {
        this.dependencyManager = dependencyManager;
        this.manifest = manifest;
        this.remoteVersion = remoteVersion;
        setSignificance(TaskSignificance.MODERATE);
    }

    @Override
    public boolean doPreExecute() {
        return true;
    }

    @Override
    public void preExecute() throws Exception {
        installer = Files.createTempFile("neoforge-installer", ".jar");

        DownloadProvider downloadProvider = dependencyManager.getDownloadProvider();
        dependent = new FileDownloadTask(
                downloadProvider.getDownloadCandidates(remoteVersion),
                installer, null
        );
        dependent.setCacheRepository(dependencyManager.getCacheRepository());
        dependent.setCaching(true);
        dependent.addIntegrityCheckHandler(FileDownloadTask.ZIP_INTEGRITY_CHECK_HANDLER);
    }

    @Override
    public boolean doPostExecute() {
        return true;
    }

    @Override
    public void postExecute() throws Exception {
        Files.deleteIfExists(installer);
        this.setResult(dependency.getResult());
    }

    @Override
    public Collection<? extends Task<?>> getDependents() {
        return Collections.singleton(dependent);
    }

    @Override
    public Collection<? extends Task<?>> getDependencies() {
        return Collections.singleton(dependency);
    }

    @Override
    public void execute() throws Exception {
        dependency = install(dependencyManager, manifest, remoteVersion.getGameVersion().toString(), installer);
    }

    /// Creates a task that installs NeoForge from a local installer JAR.
    ///
    /// The returned task obtains the matching vanilla client JAR from shared cache storage and
    /// passes it explicitly to the selected processor implementation.
    ///
    /// @param dependencyManager repository-scoped download services
    /// @param manifest          working manifest receiving the NeoForge patch
    /// @param gameVersion       Minecraft version expected by the installation
    /// @param installer         the NeoForge installer JAR
    /// @return the task producing the NeoForge patch
    /// @throws IOException              if the installer profile is missing, malformed, or
    ///                                  unsupported
    /// @throws VersionMismatchException if the installer targets another Minecraft version
    public static Task<GameInstancePatch> install(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest manifest,
            String gameVersion,
            Path installer) throws IOException, VersionMismatchException {
        try (FileSystem fs = CompressingUtils.createReadOnlyZipFileSystem(installer)) {
            String installProfileText = Files.readString(fs.getPath("install_profile.json"));
            Map<?, ?> installProfile = JsonUtils.fromNonNullJson(installProfileText, Map.class);
            if (GameComponentType.FORGE.getPatchId().equals(installProfile.get("profile")) && (Files.exists(fs.getPath("META-INF/NEOFORGE.RSA")) || installProfileText.contains("neoforge"))) {
                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion);
                return new GameDownloadTask(dependencyManager, manifest)
                        .thenComposeAsync(minecraftJar -> new ForgeNewInstallTask(
                                dependencyManager,
                                manifest,
                                minecraftJar,
                                modifyNeoForgeOldVersion(gameVersion, profile.getVersion()),
                                installer))
                        .thenApplyAsync(neoForgeVersion -> {
                    if (!neoForgeVersion.id().equals(GameComponentType.FORGE.getPatchId()) || neoForgeVersion.version() == null) {
                        throw new IOException("Invalid neoforge version.");
                    }
                    return neoForgeVersion.withId(GameComponentType.NEO_FORGE.getPatchId())
                            .withVersion(
                                    removePrefix(neoForgeVersion.version().replace(GameComponentType.FORGE.getPatchId(), ""), "-")
                            );
                });
            } else if (GameComponentType.NEO_FORGE.getPatchId().equals(installProfile.get("profile")) || "NeoForge".equals(installProfile.get("profile"))) {
                ForgeNewInstallProfile profile = JsonUtils.fromNonNullJson(installProfileText, ForgeNewInstallProfile.class);
                if (!gameVersion.equals(profile.getMinecraft()))
                    throw new VersionMismatchException(profile.getMinecraft(), gameVersion);
                return new GameDownloadTask(dependencyManager, manifest)
                        .thenComposeAsync(minecraftJar -> new NeoForgeOldInstallTask(
                                dependencyManager,
                                manifest,
                                minecraftJar,
                                modifyNeoForgeNewVersion(profile.getVersion()),
                                installer));
            } else {
                throw new IOException();
            }
        }
    }

    private static String modifyNeoForgeOldVersion(String gameVersion, String version) {
        return removeSuffix(removePrefix(removeSuffix(removePrefix(version.replace(gameVersion, "").trim(), "-"), "-"), "_"), "_");
    }

    private static String modifyNeoForgeNewVersion(String version) {
        return removePrefix(version.replace("neoforge", ""), "-");
    }
}
