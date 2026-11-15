// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import javafx.application.Platform;
import com.ddjyx.neohmcl.game.HMCLGameRepository;
import com.ddjyx.neohmcl.modpack.Modpack;
import com.ddjyx.neohmcl.modpack.server.ServerModpackManifest;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.WebPage;
import com.ddjyx.neohmcl.ui.construct.MessageDialogPane;
import com.ddjyx.neohmcl.ui.construct.RequiredValidator;
import com.ddjyx.neohmcl.ui.construct.Validator;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.StringUtils;

import java.io.IOException;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public final class RemoteModpackPage extends ModpackPage {
    private final ServerModpackManifest manifest;

    public RemoteModpackPage(WizardController controller) {
        super(controller);

        manifest = controller.getSettings().get(MODPACK_SERVER_MANIFEST);
        if (manifest == null)
            throw new IllegalStateException("MODPACK_SERVER_MANIFEST should exist");

        try {
            controller.getSettings().put(MODPACK_MANIFEST, manifest.toModpack());
        } catch (IOException e) {
            Controllers.dialog(i18n("modpack.type.server.malformed"), i18n("message.error"), MessageDialogPane.MessageType.ERROR);
            Platform.runLater(controller::onEnd);
            return;
        }

        nameProperty.set(manifest.getName());
        versionProperty.set(manifest.getVersion());
        authorProperty.set(manifest.getAuthor());

        HMCLGameRepository repository = controller.getSettings().get(ModpackPage.REPOSITORY);
        String name = controller.getSettings().get(MODPACK_NAME);
        if (name != null) {
            txtModpackName.setText(name);
            txtModpackName.setDisable(true);
        } else {
            txtModpackName.setText(LocalModpackPage.processModpackName(manifest.getName()));
            txtModpackName.getValidators().addAll(
                    new RequiredValidator(),
                    new Validator(i18n("install.new_game.already_exists"), str -> !repository.instanceIdConflicts(str)),
                    new Validator(i18n("install.new_game.malformed"), HMCLGameRepository::isValidInstanceId));
        }

        btnDescription.setVisible(StringUtils.isNotBlank(manifest.getDescription()));
    }

    @Override
    public void cleanup(SettingsMap settings) {
        settings.remove(MODPACK_SERVER_MANIFEST);
    }

    protected void onInstall() {
        if (!txtModpackName.validate()) return;
        controller.getSettings().put(MODPACK_NAME, txtModpackName.getText());
        controller.onFinish();
    }

    protected void onDescribe() {
        Controllers.navigate(new WebPage(i18n("modpack.description"), manifest.getDescription()));
    }

    public static final SettingsMap.Key<ServerModpackManifest> MODPACK_SERVER_MANIFEST = new SettingsMap.Key<>("MODPACK_SERVER_MANIFEST");
    public static final SettingsMap.Key<String> MODPACK_NAME = new SettingsMap.Key<>("MODPACK_NAME");
    public static final SettingsMap.Key<Modpack> MODPACK_MANIFEST = new SettingsMap.Key<>("MODPACK_MANIFEST");
}
