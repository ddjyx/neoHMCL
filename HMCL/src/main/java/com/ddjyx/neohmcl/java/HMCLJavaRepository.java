// SPDX-FileCopyrightText: Copyright (C) 2024 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.java;

import com.ddjyx.neohmcl.download.DownloadProvider;
import com.ddjyx.neohmcl.download.java.mojang.MojangJavaDownloadTask;
import com.ddjyx.neohmcl.download.java.mojang.MojangJavaRemoteFiles;
import com.ddjyx.neohmcl.game.DownloadInfo;
import com.ddjyx.neohmcl.game.GameJavaVersion;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.FileUtils;
import com.ddjyx.neohmcl.util.platform.OperatingSystem;
import com.ddjyx.neohmcl.util.platform.Platform;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

/**
 * @author Glavo
 */
public final class HMCLJavaRepository implements JavaRepository {
    public static final String MOJANG_JAVA_PREFIX = "mojang-";

    private final Path root;

    public HMCLJavaRepository(Path root) {
        this.root = root;
    }

    public Path getPlatformRoot(Platform platform) {
        return root.resolve(platform.toString());
    }

    @Override
    public Path getJavaDir(Platform platform, String name) {
        return getPlatformRoot(platform).resolve(name);
    }

    public Path getJavaDir(Platform platform, GameJavaVersion gameJavaVersion) {
        return getJavaDir(platform, MOJANG_JAVA_PREFIX + gameJavaVersion.component());
    }

    @Override
    public Path getManifestFile(Platform platform, String name) {
        return getPlatformRoot(platform).resolve(name + ".json");
    }

    public Path getManifestFile(Platform platform, GameJavaVersion gameJavaVersion) {
        return getManifestFile(platform, MOJANG_JAVA_PREFIX + gameJavaVersion.component());
    }

    public boolean isInstalled(Platform platform, String name) {
        return Files.exists(getManifestFile(platform, name));
    }

    public boolean isInstalled(Platform platform, GameJavaVersion gameJavaVersion) {
        return isInstalled(platform, MOJANG_JAVA_PREFIX + gameJavaVersion.component());
    }

    public @Nullable Path getJavaExecutable(Platform platform, String name) {
        Path javaDir = getJavaDir(platform, name);
        try {
            return JavaManager.getExecutable(javaDir).toRealPath();
        } catch (IOException ignored) {
            if (platform.getOperatingSystem() == OperatingSystem.MACOS) {
                try {
                    return JavaManager.getMacExecutable(javaDir).toRealPath();
                } catch (IOException ignored1) {
                }
            }
        }

        return null;
    }

    public @Nullable Path getJavaExecutable(Platform platform, GameJavaVersion gameJavaVersion) {
        return getJavaExecutable(platform, MOJANG_JAVA_PREFIX + gameJavaVersion.component());
    }

    private static void getAllJava(List<Path> list, Platform platform, Path platformRoot) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(platformRoot)) {
            for (Path file : stream) {
                try {
                    String name = file.getFileName().toString();
                    if (name.endsWith(".json") && Files.isRegularFile(file)) {
                        Path javaDir = file.resolveSibling(name.substring(0, name.length() - ".json".length()));
                        Path executable;
                        try {
                            executable = JavaManager.getExecutable(javaDir).toRealPath();
                        } catch (IOException e) {
                            if (platform.getOperatingSystem() == OperatingSystem.MACOS)
                                executable = JavaManager.getMacExecutable(javaDir).toRealPath();
                            else
                                throw e;
                        }

                        if (Files.isDirectory(javaDir)) {
                            list.add(executable);
                        }
                    }
                } catch (Throwable e) {
                    LOG.warning("Failed to parse " + file, e);
                }
            }

        } catch (IOException ignored) {
        }
    }

    @Override
    public Collection<Path> getAllJava(Platform platform) {
        Path platformRoot = getPlatformRoot(platform);
        if (!Files.isDirectory(platformRoot))
            return Collections.emptyList();

        ArrayList<Path> list = new ArrayList<>();

        getAllJava(list, platform, platformRoot);
        return list;
    }

    @Override
    public Task<JavaRuntime> getDownloadJavaTask(DownloadProvider downloadProvider, Platform platform, GameJavaVersion gameJavaVersion) {
        Path javaDir = getJavaDir(platform, gameJavaVersion);
        Path tempDir = getPlatformRoot(platform).resolve(".tmp").resolve(javaDir.getFileName());

        return new MojangJavaDownloadTask(downloadProvider, javaDir, tempDir, gameJavaVersion, JavaManager.getMojangJavaPlatform(platform)).thenApplyAsync(result -> {
            Path executable;
            try {
                executable = JavaManager.getExecutable(javaDir).toRealPath();
            } catch (IOException e) {
                if (platform.getOperatingSystem() == OperatingSystem.MACOS)
                    executable = JavaManager.getMacExecutable(javaDir).toRealPath();
                else
                    throw e;
            }

            JavaInfo info;
            if (JavaManager.isCompatible(platform))
                info = JavaInfoUtils.fromExecutable(executable);
            else
                info = new JavaInfo(platform, result.download().version().name(), null);

            Map<String, Object> update = new LinkedHashMap<>();
            update.put("provider", "mojang");
            update.put("component", gameJavaVersion.component());

            Map<String, JavaLocalFiles.Local> files = new LinkedHashMap<>();
            result.remoteFiles().files().forEach((path, file) -> {
                if (file instanceof MojangJavaRemoteFiles.RemoteFile) {
                    DownloadInfo downloadInfo = ((MojangJavaRemoteFiles.RemoteFile) file).getDownloads().get("raw");
                    if (downloadInfo != null) {
                        files.put(path, new JavaLocalFiles.LocalFile(downloadInfo.getSha1(), downloadInfo.getSize()));
                    }
                } else if (file instanceof MojangJavaRemoteFiles.RemoteDirectory) {
                    files.put(path, new JavaLocalFiles.LocalDirectory());
                } else if (file instanceof MojangJavaRemoteFiles.RemoteLink) {
                    files.put(path, new JavaLocalFiles.LocalLink(((MojangJavaRemoteFiles.RemoteLink) file).getTarget()));
                }
            });

            JavaManifest manifest = new JavaManifest(info, update, files);
            JsonUtils.writeToJsonFile(getManifestFile(platform, gameJavaVersion), manifest);
            return JavaRuntime.of(executable, info, true);
        });
    }

    public Task<JavaRuntime> getInstallJavaTask(Platform platform, String name, Map<String, Object> update, Path archiveFile) {
        Path javaDir = getJavaDir(platform, name);
        return new JavaInstallTask(javaDir, update, archiveFile).thenApplyAsync(result -> {
            if (!result.info().getPlatform().equals(platform))
                throw new IOException("Platform is mismatch: expected " + platform + " but got " + result.info().getPlatform());

            Path executable = javaDir.resolve("bin").resolve(platform.getOperatingSystem().getJavaExecutable()).toRealPath();
            JsonUtils.writeToJsonFile(getManifestFile(platform, name), result);
            return JavaRuntime.of(executable, result.info(), true);
        });
    }

    @Override
    public Task<Void> getUninstallJavaTask(Platform platform, String name) {
        return Task.runAsync(() -> {
            Files.deleteIfExists(getManifestFile(platform, name));
            FileUtils.deleteDirectory(getJavaDir(platform, name));
        });
    }

    @Override
    public Task<Void> getUninstallJavaTask(JavaRuntime java) {
        return Task.runAsync(() -> {
            Path root = getPlatformRoot(java.getPlatform());
            Path relativized = root.relativize(java.getBinary());

            if (relativized.getNameCount() > 1) {
                String name = relativized.getName(0).toString();
                Files.deleteIfExists(getManifestFile(java.getPlatform(), name));
                FileUtils.deleteDirectory(getJavaDir(java.getPlatform(), name));
            }
        });
    }
}
