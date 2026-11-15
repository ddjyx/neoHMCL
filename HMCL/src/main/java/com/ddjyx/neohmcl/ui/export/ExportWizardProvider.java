// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.export;

import javafx.scene.Node;
import com.ddjyx.neohmcl.Metadata;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.modpack.ModAdviser;
import com.ddjyx.neohmcl.modpack.ModpackExportInfo;
import com.ddjyx.neohmcl.modpack.mcbbs.McbbsModpackExportTask;
import com.ddjyx.neohmcl.modpack.modrinth.ModrinthModpackExportTask;
import com.ddjyx.neohmcl.modpack.server.ServerModpackExportTask;
import com.ddjyx.neohmcl.setting.AuthlibInjectorServerList;
import com.ddjyx.neohmcl.setting.FontManager;
import com.ddjyx.neohmcl.setting.LauncherSettings;
import com.ddjyx.neohmcl.setting.SettingsManager;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.ui.wizard.WizardProvider;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.io.JarUtils;
import com.ddjyx.neohmcl.util.io.Zipper;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.ddjyx.neohmcl.setting.SettingsManager.settings;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public final class ExportWizardProvider implements WizardProvider {
    private final HMCLGameInstance gameInstance;

    public ExportWizardProvider(HMCLGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @Override
    public void start(SettingsMap settings) {
    }

    @Override
    public Object finish(SettingsMap settings) {
        List<String> whitelist = settings.get(ModpackFileSelectionPage.MODPACK_FILE_SELECTION);
        Path modpackFile = settings.get(ModpackFileSelectionPage.MODPACK_FILE);
        ModpackExportInfo exportInfo = Objects.requireNonNull(settings.get(ModpackInfoPage.MODPACK_INFO));
        exportInfo.setWhitelist(whitelist);
        String modpackType = settings.get(ModpackTypeSelectionPage.MODPACK_TYPE);

        settings.put("title", i18n("install.modpack.export", exportInfo.getName()));
        return exportWithLauncher(modpackType, exportInfo, modpackFile);
    }

    private Task<?> exportWithLauncher(String modpackType, ModpackExportInfo exportInfo, Path modpackFile) {
        @Nullable Path launcherJar = JarUtils.thisJarPath();
        boolean packWithLauncher = exportInfo.isPackWithLauncher() && launcherJar != null;
        return new Task<>() {
            @Nullable Path tempModpack;
            @Nullable Task<?> exportTask;

            {
                setSignificance(TaskSignificance.MODERATE);
            }

            @Override
            public boolean doPreExecute() {
                return true;
            }

            @Override
            public void preExecute() throws Exception {
                Path dest;
                if (packWithLauncher) {
                    dest = tempModpack = Files.createTempFile("hmcl", ".zip");
                } else {
                    dest = modpackFile;
                }

                switch (modpackType) {
                    case ModpackTypeSelectionPage.MODPACK_TYPE_MCBBS:
                        exportTask = exportAsMcbbs(exportInfo, dest);
                        break;
                    case ModpackTypeSelectionPage.MODPACK_TYPE_SERVER:
                        exportTask = exportAsServer(exportInfo, dest);
                        break;
                    case ModpackTypeSelectionPage.MODPACK_TYPE_MODRINTH:
                        exportTask = exportAsModrinth(exportInfo, dest);
                        break;
                    default:
                        throw new IllegalStateException("Unrecognized modpack type " + modpackType);
                }

            }

            @Override
            public Collection<Task<?>> getDependents() {
                return Collections.singleton(exportTask);
            }

            @Override
            public void execute() throws Exception {
                if (!packWithLauncher) return;
                try (Zipper zip = new Zipper(modpackFile)) {
                    LauncherSettings exported = new LauncherSettings();
                    LauncherSettings current = settings();

                    exported.versionListSourceProperty().set(current.versionListSourceProperty().get());
                    exported.fileDownloadSourceProperty().set(current.fileDownloadSourceProperty().get());
                    exported.preferredLoginTypeProperty().set(current.preferredLoginTypeProperty().get());

                    zip.putTextFile(exported.toJson(), ".hmcl/config/launcher-settings.json");
                    AuthlibInjectorServerList exportedServers = new AuthlibInjectorServerList();
                    exportedServers.getServers().setAll(SettingsManager.getAuthlibInjectorServers());
                    zip.putTextFile(
                            JsonUtils.GSON.toJson(exportedServers, AuthlibInjectorServerList.class),
                            ".hmcl/config/authlib-injector-servers.json");

                    // Bundled package under .hmcl/modpack/ (not the process workdir).
                    String packageName = ModpackTypeSelectionPage.MODPACK_TYPE_MODRINTH.equals(modpackType)
                            ? "modpack.mrpack"
                            : "modpack.zip";
                    zip.putFile(tempModpack, ".hmcl/" + Metadata.BUNDLED_MODPACK_DIRECTORY_NAME + "/" + packageName);

                    for (String extension : FontManager.FONT_EXTENSIONS) {
                        String fileName = "font." + extension;
                        Path font = Metadata.HMCL_LOCAL_HOME.resolve(fileName);
                        if (!Files.isRegularFile(font))
                            font = Metadata.CURRENT_DIRECTORY.resolve(fileName);
                        if (Files.isRegularFile(font))
                            zip.putFile(font, ".hmcl/" + fileName);
                    }

                    zip.putFile(launcherJar, launcherJar.getFileName().toString());
                } finally {
                    if (tempModpack != null) {
                        try {
                            Files.deleteIfExists(tempModpack);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        };
    }

    private Task<?> exportAsMcbbs(ModpackExportInfo exportInfo, Path modpackFile) {
        return new Task<Void>() {
            @Nullable Task<?> dependency;

            {
                setSignificance(TaskSignificance.MODERATE);
            }

            @Override
            public void execute() {
                dependency = new McbbsModpackExportTask(resolveCurrentGameInstance(), exportInfo, modpackFile);
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    private Task<?> exportAsServer(ModpackExportInfo exportInfo, Path modpackFile) {
        return new Task<Void>() {
            @Nullable Task<?> dependency;

            {
                setSignificance(TaskSignificance.MODERATE);
            }

            @Override
            public void execute() {
                dependency = new ServerModpackExportTask(resolveCurrentGameInstance(), exportInfo, modpackFile);
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    private Task<?> exportAsModrinth(ModpackExportInfo exportInfo, Path modpackFile) {
        return new Task<Void>() {
            @Nullable Task<?> dependency;

            {
                setSignificance(TaskSignificance.MODERATE);
            }

            @Override
            public void execute() {
                dependency = new ModrinthModpackExportTask(
                        resolveCurrentGameInstance(),
                        exportInfo,
                        modpackFile
                );
            }

            @Override
            public Collection<Task<?>> getDependencies() {
                return Collections.singleton(dependency);
            }
        };
    }

    /// Returns the current registered snapshot for the instance selected by this wizard.
    ///
    /// @return the current registered instance
    private HMCLGameInstance resolveCurrentGameInstance() {
        return gameInstance.getRepository().getInstance(gameInstance.getId());
    }

    @Override
    public Node createPage(WizardController controller, int step, SettingsMap settings) {
        return switch (step) {
            case 0 -> new ModpackTypeSelectionPage(controller);
            case 1 -> new ModpackInfoPage(controller, gameInstance);
            case 2 -> new ModpackFileSelectionPage(controller, gameInstance, ModAdviser::suggestMod);
            default -> throw new IllegalArgumentException("step");
        };
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
