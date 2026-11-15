// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.export;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import com.ddjyx.neohmcl.modpack.ModpackExportInfo;
import com.ddjyx.neohmcl.modpack.mcbbs.McbbsModpackExportTask;
import com.ddjyx.neohmcl.modpack.modrinth.ModrinthModpackExportTask;
import com.ddjyx.neohmcl.modpack.server.ServerModpackExportTask;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.construct.TwoLineListItem;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.ui.wizard.WizardPage;
import com.ddjyx.neohmcl.util.SettingsMap;

import static com.ddjyx.neohmcl.ui.export.ModpackInfoPage.MODPACK_INFO_OPTION;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public final class ModpackTypeSelectionPage extends VBox implements WizardPage {
    private final WizardController controller;

    public ModpackTypeSelectionPage(WizardController controller) {
        this.controller = controller;
        this.setPadding(new Insets(10));

        Label title = new Label(i18n("modpack.export.as"));
        VBox.setMargin(title, new Insets(8, 0, 8, 12));

        this.getStyleClass().add("jfx-list-view");
        this.setMaxSize(400, 150);
        this.setSpacing(8);

        this.getChildren().setAll(
                title,
                createButton(MODPACK_TYPE_MCBBS, McbbsModpackExportTask.OPTION),
                createButton(MODPACK_TYPE_MODRINTH, ModrinthModpackExportTask.OPTION),
                createButton(MODPACK_TYPE_SERVER, ServerModpackExportTask.OPTION)
        );
    }

    private JFXButton createButton(String type, ModpackExportInfo.Options option) {
        JFXButton button = new JFXButton();

        button.getStyleClass().add("card");
        button.setOnAction(e -> {
            controller.getSettings().put(MODPACK_TYPE, type);
            controller.getSettings().put(MODPACK_INFO_OPTION, option);
            controller.onNext();
        });

        button.prefWidthProperty().bind(this.widthProperty());

        BorderPane graphic = new BorderPane();
        graphic.setMouseTransparent(true);
        graphic.setLeft(new TwoLineListItem(i18n("modpack.type." + type), i18n("modpack.type." + type + ".export")));

        Node arrow = SVG.ARROW_FORWARD.createIcon();
        BorderPane.setAlignment(arrow, Pos.CENTER);
        graphic.setRight(arrow);

        button.setGraphic(graphic);

        return button;
    }

    @Override
    public void cleanup(SettingsMap settings) {
    }

    @Override
    public String getTitle() {
        return i18n("modpack.wizard.step.3.title");
    }

    public static final SettingsMap.Key<String> MODPACK_TYPE = new SettingsMap.Key<>("modpack.type");

    public static final String MODPACK_TYPE_MCBBS = "mcbbs";
    public static final String MODPACK_TYPE_SERVER = "server";
    public static final String MODPACK_TYPE_MODRINTH = "modrinth";
}
