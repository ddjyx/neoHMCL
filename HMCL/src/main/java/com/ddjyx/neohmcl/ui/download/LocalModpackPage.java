// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;
import com.ddjyx.neohmcl.game.HMCLGameRepository;
import com.ddjyx.neohmcl.game.ManuallyCreatedModpackException;
import com.ddjyx.neohmcl.game.ModpackHelper;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.ModpackFile;
import com.ddjyx.neohmcl.modpack.ModpackManifest;
import com.ddjyx.neohmcl.setting.DownloadProviders;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.WebPage;
import com.ddjyx.neohmcl.ui.construct.MessageDialogPane;
import com.ddjyx.neohmcl.ui.construct.RequiredValidator;
import com.ddjyx.neohmcl.ui.construct.Validator;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

public final class LocalModpackPage extends ModpackPage {

    private final BooleanProperty installAsVersion = new SimpleBooleanProperty(true);
    private Modpack manifest = null;
    private final ObservableList<ModpackFile> allFiles = FXCollections.observableList(new ArrayList<>());
    private final ObservableSet<String> excludedFiles = FXCollections.observableSet(new HashSet<>());
    private final BooleanProperty loadingOptionalFiles = new SimpleBooleanProperty(false);
    private final BooleanProperty loadedOptionalFiles = new SimpleBooleanProperty(true);

    public LocalModpackPage(WizardController controller) {
        super(controller);

        btnOptionalFiles.setOnAction(ev -> controller.onNext(new OptionalFilesPage(
                this::onInstall,
                this::loadOptionalFiles,
                loadingOptionalFiles,
                loadedOptionalFiles,
                new FilteredList<>(allFiles, ModpackFile::optional),
                excludedFiles)));

        HMCLGameRepository repository = controller.getSettings().get(ModpackPage.REPOSITORY);

        String name = controller.getSettings().get(MODPACK_NAME);
        if (name != null) {
            txtModpackName.setText(name);
            txtModpackName.setDisable(true);
        } else {
            FXUtils.onChangeAndOperate(installAsVersion, installAsVersion -> {
                if (installAsVersion) {
                    txtModpackName.getValidators().setAll(
                            new RequiredValidator(),
                            new Validator(i18n("install.new_game.already_exists"), str -> !repository.instanceIdConflicts(str)),
                            new Validator(i18n("install.new_game.malformed"), HMCLGameRepository::isValidInstanceId));
                } else {
                    txtModpackName.getValidators().setAll(
                            new RequiredValidator(),
                            new Validator(i18n("install.new_game.already_exists"), str -> !ModpackHelper.isExternalGameNameConflicts(str)
                                    && GameDirectoryManager.getGameDirectories().stream()
                                    .noneMatch(existingProfile ->
                                            str.equals(GameDirectoryManager.getGameDirectoryCustomName(existingProfile)))),
                            new Validator(i18n("install.new_game.malformed"), HMCLGameRepository::isValidInstanceId));
                }
            });
        }

        btnDescription.setVisible(false);

        Path selectedFile;
        Path filePath = controller.getSettings().get(MODPACK_FILE);
        if (filePath != null) {
            selectedFile = filePath;
        } else {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(i18n("modpack.choose"));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("modpack"), "*.zip"));
            selectedFile = Controllers.showOpenDialog(chooser);
            if (selectedFile == null) {
                controller.onEnd();
                return;
            }

            controller.getSettings().put(MODPACK_FILE, selectedFile);
        }

        showSpinner();
        Task.supplyAsync(() -> ModpackHelper.readModpackManifest(selectedFile))
                .whenComplete(Schedulers.javafx(), (manifest, exception) -> {
                    if (exception instanceof ManuallyCreatedModpackException) {
                        hideSpinner();
                        nameProperty.set(FileUtils.getName(selectedFile));
                        installAsVersion.set(false);

                        if (name == null) {
                            // trim: https://github.com/HMCL-dev/HMCL/issues/962
                            txtModpackName.setText(FileUtils.getNameWithoutExtension(selectedFile));
                        }

                        Controllers.confirm(i18n("modpack.type.manual.warning"), i18n("install.modpack"), MessageDialogPane.MessageType.WARNING,
                                () -> {
                                },
                                controller::onEnd);

                        controller.getSettings().put(MODPACK_MANUALLY_CREATED, true);
                    } else if (exception != null) {
                        LOG.warning("Failed to read modpack manifest", exception);
                        Controllers.dialog(i18n("modpack.task.install.error"), i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                        Platform.runLater(controller::onEnd);
                    } else if (manifest == null) {
                        Controllers.dialog(i18n("modpack.task.install.error"), i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                        Platform.runLater(controller::onEnd);
                    } else {
                        this.manifest = manifest;
                        hideSpinner();
                        controller.getSettings().put(MODPACK_MANIFEST, manifest);
                        nameProperty.set(manifest.getName());
                        versionProperty.set(manifest.getVersion());
                        authorProperty.set(manifest.getAuthor());

                        if (name == null) {
                            // trim: https://github.com/HMCL-dev/HMCL/issues/962
                            txtModpackName.setText(processModpackName(manifest.getName()));
                        }

                        btnDescription.setVisible(StringUtils.isNotBlank(manifest.getDescription()));

                        if (manifest.getManifest() instanceof ModpackManifest.SupportOptional supportOptional) {
                            allFiles.setAll(supportOptional.getFiles());
                            if (allFiles.stream().anyMatch(ModpackFile::optional)) {
                                loadOptionalFiles();
                                btnOptionalFiles.setVisible(true);
                                btnOptionalFiles.setManaged(true);
                            }
                        }
                    }
                }).start();
    }

    private void loadOptionalFiles() {
        Objects.requireNonNull(manifest);
        loadingOptionalFiles.set(true);
        loadedOptionalFiles.set(false);
        Task.supplyAsync(() -> manifest.getManifest().getProvider().loadFiles(DownloadProviders.getDownloadProvider(), manifest.getManifest()))
                .whenComplete(Schedulers.javafx(), (manifest1, exception) -> {
                    loadingOptionalFiles.set(false);
                    List<? extends ModpackFile> files = ((ModpackManifest.SupportOptional) manifest1).getFiles();
                    manifest.setManifest(manifest1);
                    allFiles.setAll(files);
                    if (files.stream().anyMatch(s -> s.optional() && (!s.addonQueried() || s.fileName() == null))) {
                        LOG.warning("Failed to load optional files");
                    } else {
                        loadedOptionalFiles.set(true);
                    }
                }).start();
    }

    @Override
    public void cleanup(SettingsMap settings) {
        settings.remove(MODPACK_FILE);
    }

    protected void onInstall() {
        String name = txtModpackName.getText();

        // Check for non-ASCII characters.
        if (!StringUtils.isASCII(name)) {
            Controllers.dialog(new MessageDialogPane.Builder(
                    i18n("install.name.invalid"),
                    i18n("message.warning"),
                    MessageDialogPane.MessageType.QUESTION)
                    .yesOrNo(() -> finishInstall(name), () -> {
                        // The user selects Cancel and does nothing.
                    })
                    .build());
        } else {
            finishInstall(name);
        }
    }

    private void finishInstall(String name) {
        controller.getSettings().put(MODPACK_NAME, name);
        controller.getSettings().put(MODPACK_EXCLUDED_FILES, getExcludedFiles());
        controller.onFinish();
    }

    private @Nullable Set<String> getExcludedFiles() {
        if (allFiles.isEmpty())
            return null;
        return Set.copyOf(excludedFiles);
    }

    protected void onDescribe() {
        if (manifest != null)
            Controllers.navigate(new WebPage(i18n("modpack.description"), manifest.getDescription()));
    }

    @Contract("null->null;!null->!null")
    public static @Nullable String processModpackName(@Nullable String name) {
        if (name == null)
            return null;

        return name.trim()
                .replace(':', '_')
                .replace('/', '_')
                .replace('\\', '_');
    }

    public static final SettingsMap.Key<Path> MODPACK_FILE = new SettingsMap.Key<>("MODPACK_FILE");
    public static final SettingsMap.Key<String> MODPACK_NAME = new SettingsMap.Key<>("MODPACK_NAME");
    public static final SettingsMap.Key<Modpack> MODPACK_MANIFEST = new SettingsMap.Key<>("MODPACK_MANIFEST");
    public static final SettingsMap.Key<Boolean> MODPACK_MANUALLY_CREATED = new SettingsMap.Key<>("MODPACK_MANUALLY_CREATED");
    public static final SettingsMap.Key<String> MODPACK_ICON_URL = new SettingsMap.Key<>("MODPACK_ICON_URL");
    public static final SettingsMap.Key<Set<String>> MODPACK_EXCLUDED_FILES = new SettingsMap.Key<>("MODPACK_EXCLUDED_FILES");
}
