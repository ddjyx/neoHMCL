// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.ddjyx.neohmcl.game.HMCLGameRepository;
import com.ddjyx.neohmcl.setting.GameDirectory;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.construct.ComponentList;
import com.ddjyx.neohmcl.ui.construct.LinePane;
import com.ddjyx.neohmcl.ui.construct.LineTextPane;
import com.ddjyx.neohmcl.ui.construct.SpinnerPane;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.ui.wizard.WizardPage;
import com.ddjyx.neohmcl.util.SettingsMap;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public abstract class ModpackPage extends SpinnerPane implements WizardPage {
    public static final SettingsMap.Key<GameDirectory> GAME_DIRECTORY = new SettingsMap.Key<>("GAME_DIRECTORY");
    public static final SettingsMap.Key<HMCLGameRepository> REPOSITORY = new SettingsMap.Key<>("REPOSITORY");

    protected final WizardController controller;

    protected final StringProperty nameProperty;
    protected final StringProperty versionProperty;
    protected final StringProperty authorProperty;
    protected final JFXTextField txtModpackName;
    protected final JFXButton btnInstall;
    protected final JFXButton btnDescription;
    protected final JFXButton btnOptionalFiles;

    protected ModpackPage(WizardController controller) {
        this.controller = controller;

        VBox borderPane = new VBox();
        borderPane.setAlignment(Pos.CENTER);
        FXUtils.setLimitWidth(borderPane, 500);

        ComponentList componentList = new ComponentList();
        {
            var archiveNamePane = new LinePane();
            {
                FXUtils.setLimitHeight(archiveNamePane, 75);
                archiveNamePane.setTitle(i18n("instance.name"));

                txtModpackName = new JFXTextField();
                txtModpackName.setPrefWidth(300);
                BorderPane.setAlignment(txtModpackName, Pos.CENTER_RIGHT);
                archiveNamePane.setRight(txtModpackName);
            }

            var modpackNamePane = new LineTextPane();
            {
                modpackNamePane.setTitle(i18n("modpack.name"));
                nameProperty = modpackNamePane.textProperty();
            }

            var versionPane = new LineTextPane();
            {
                versionPane.setTitle(i18n("archive.version"));
                versionProperty = versionPane.textProperty();
            }

            var authorPane = new LineTextPane();
            {
                authorPane.setTitle(i18n("archive.author"));
                authorProperty = authorPane.textProperty();
            }

            var descriptionPane = new BorderPane();
            {
                btnDescription = FXUtils.newBorderButton(i18n("modpack.description"));
                btnDescription.setOnAction(e -> onDescribe());
                descriptionPane.setLeft(btnDescription);

                var installHBox = new HBox(8);
                btnOptionalFiles = FXUtils.newRaisedButton(i18n("modpack.optional_files"));
                btnOptionalFiles.setVisible(false);
                btnOptionalFiles.setManaged(false);
                installHBox.getChildren().add(btnOptionalFiles);

                btnInstall = FXUtils.newRaisedButton(i18n("button.install"));
                btnInstall.setOnAction(e -> onInstall());
                var nameInvalid = createBooleanBinding(() -> !txtModpackName.validate(), txtModpackName.textProperty());
                btnInstall.disableProperty().bind(nameInvalid);
                btnOptionalFiles.disableProperty().bind(nameInvalid);
                installHBox.getChildren().add(btnInstall);
                descriptionPane.setRight(installHBox);
            }

            componentList.getContent().setAll(
                    archiveNamePane, modpackNamePane, versionPane, authorPane, descriptionPane);
        }

        borderPane.getChildren().setAll(componentList);
        this.setContent(borderPane);
    }

    protected abstract void onInstall();

    protected abstract void onDescribe();

    @Override
    public String getTitle() {
        return i18n("modpack.task.install");
    }
}
