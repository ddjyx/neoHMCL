// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.glavo.url.WebURL;
import com.ddjyx.neohmcl.game.GameInstanceID;
import com.ddjyx.neohmcl.game.ModpackHelper;
import com.ddjyx.neohmcl.modpack.server.ServerModpackManifest;
import com.ddjyx.neohmcl.task.FileDownloadTask;
import com.ddjyx.neohmcl.task.GetTask;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.construct.TwoLineListItem;
import com.ddjyx.neohmcl.ui.construct.URLValidator;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.ui.wizard.WizardPage;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.TaskCancellationAction;
import com.ddjyx.neohmcl.util.gson.JsonUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.ddjyx.neohmcl.ui.download.LocalModpackPage.MODPACK_FILE;
import static com.ddjyx.neohmcl.ui.download.LocalModpackPage.MODPACK_NAME;
import static com.ddjyx.neohmcl.ui.download.RemoteModpackPage.MODPACK_SERVER_MANIFEST;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public final class ModpackSelectionPage extends VBox implements WizardPage {
    private final WizardController controller;

    public ModpackSelectionPage(WizardController controller) {
        this.controller = controller;

        Label title = new Label(i18n("install.modpack"));
        title.setPadding(new Insets(8));

        this.getStyleClass().add("jfx-list-view");
        this.setMaxSize(400, 150);
        this.setSpacing(8);
        this.getChildren().setAll(
                title,
                createButton("local", this::onChooseLocalFile),
                createButton("remote", this::onChooseRemoteFile),
                createButton("repository", this::onChooseRepository)
        );

        Path filePath = controller.getSettings().get(MODPACK_FILE);
        if (filePath != null) {
            controller.getSettings().put(MODPACK_FILE, filePath);
            Platform.runLater(controller::onNext);
        }

        FXUtils.applyDragListener(this, ModpackHelper::isFileModpackByExtension, modpacks -> {
            Path modpack = modpacks.get(0);
            controller.getSettings().put(MODPACK_FILE, modpack);
            controller.onNext();
        });
    }

    private JFXButton createButton(String type, Runnable action) {
        JFXButton button = new JFXButton();

        button.getStyleClass().add("card");
        button.setStyle("-fx-cursor: HAND;");
        button.prefWidthProperty().bind(this.widthProperty());
        button.setOnAction(e -> action.run());

        BorderPane graphic = new BorderPane();
        graphic.setMouseTransparent(true);
        graphic.setLeft(new TwoLineListItem(i18n("modpack.choose." + type), i18n("modpack.choose." + type + ".detail")));

        SVGPath arrow = SVG.ARROW_FORWARD.createIcon();
        BorderPane.setAlignment(arrow, Pos.CENTER);
        graphic.setRight(arrow);

        button.setGraphic(graphic);

        JFXDepthManager.setDepth(button, 1);

        return button;
    }

    private void onChooseLocalFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("modpack.choose"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("modpack"), "*.zip", "*.mrpack"));
        Path selectedFile = Controllers.showOpenDialog(chooser);
        if (selectedFile == null) {
            return;
        }

        controller.getSettings().put(MODPACK_FILE, selectedFile);
        controller.onNext();
    }

    private void onChooseRemoteFile() {
        Controllers.prompt(i18n("modpack.choose.remote.tooltip"), (url, handler) -> {
            try {
                if (url.endsWith("server-manifest.json")) {
                    // if urlString ends with .json, we assume that the url is server-manifest.json
                    Controllers.taskDialog(new GetTask(WebURL.parse(url)).whenComplete(Schedulers.javafx(), (result, e) -> {
                        ServerModpackManifest manifest = JsonUtils.fromMaybeMalformedJson(result, ServerModpackManifest.class);
                        if (manifest == null) {
                            handler.reject(i18n("modpack.type.server.malformed"));
                        } else if (e == null) {
                            handler.resolve();
                            controller.getSettings().put(MODPACK_SERVER_MANIFEST, manifest);
                            controller.onNext();
                        } else {
                            handler.reject(e.getMessage());
                        }
                    }), i18n("message.downloading"), TaskCancellationAction.NORMAL);
                } else {
                    // otherwise we still consider the file as modpack zip file
                    // since casually the url may not ends with ".zip"
                    Path modpack = Files.createTempFile("modpack", ".zip");
                    Controllers.taskDialog(
                            new FileDownloadTask(url, modpack)
                                    .whenComplete(Schedulers.javafx(), e -> {
                                        if (e == null) {
                                            handler.resolve();
                                            controller.getSettings().put(MODPACK_FILE, modpack);
                                            controller.onNext();
                                        } else {
                                            handler.reject(e.getMessage());
                                        }
                                    }),
                            i18n("message.downloading"),
                            TaskCancellationAction.NORMAL
                    );
                }
            } catch (Exception e) {
                handler.reject(i18n("message.failed"));
            }
        }, "", new URLValidator());
    }

    public void onChooseRepository() {
        String modPackName = controller.getSettings().get(MODPACK_NAME);
        DownloadPage downloadPage = new DownloadPage(modPackName != null ? new GameInstanceID(modPackName) : null);
        downloadPage.showModpackDownloads();
        Controllers.navigate(downloadPage);
    }

    @Override
    public void cleanup(SettingsMap settings) {
    }

    @Override
    public String getTitle() {
        return i18n("modpack.task.install");
    }
}
