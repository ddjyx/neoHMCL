// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import com.ddjyx.neohmcl.download.DownloadProvider;
import com.ddjyx.neohmcl.download.ComponentRemoteVersion;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceID;
import com.ddjyx.neohmcl.game.HMCLGameRepository;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.InstallerItem;
import com.ddjyx.neohmcl.ui.construct.MessageDialogPane;
import com.ddjyx.neohmcl.ui.construct.RequiredValidator;
import com.ddjyx.neohmcl.ui.construct.Validator;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.i18n.I18n;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;

import static javafx.beans.binding.Bindings.createBooleanBinding;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public class InstallersPage extends AbstractInstallersPage {

    private boolean isNameModifiedByUser = false;

    public InstallersPage(WizardController controller, HMCLGameRepository repository, GameVersionNumber gameVersion, DownloadProvider downloadProvider) {
        super(controller, gameVersion, downloadProvider);

        txtName.getValidators().addAll(
                new RequiredValidator(),
                new Validator(i18n("install.new_game.already_exists"), str -> !repository.instanceIdConflicts(str)),
                new Validator(i18n("install.new_game.malformed"), HMCLGameRepository::isValidInstanceId));
        installable.bind(createBooleanBinding(txtName::validate, txtName.textProperty()));

        txtName.textProperty().addListener((obs, oldText, newText) -> isNameModifiedByUser = true);
    }

    @Override
    public String getTitle() {
        return ((ComponentRemoteVersion) controller.getSettings().get("game")).getGameVersion().toString();
    }

    private String getVersion(String id) {
        return I18n.getDisplayVersion((ComponentRemoteVersion) controller.getSettings().get(id));
    }

    protected void reload() {
        for (InstallerItem component : group.getComponents()) {
            String libraryId = component.getComponentType().getPatchId();
            if (controller.getSettings().containsKey(libraryId)) {
                component.versionProperty().set(new InstallerItem.InstalledState(getVersion(libraryId), false, false));
            } else {
                component.versionProperty().set(null);
            }
        }
        if (!isNameModifiedByUser) {
            setTxtNameWithLoaders();
        }
    }

    @Override
    public void cleanup(SettingsMap settings) {
    }

    private static boolean checkName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= '0' && c <= '9')
                    && !(c >= 'a' && c <= 'z')
                    && !(c >= 'A' && c <= 'Z')
                    && c != '-' && c != '_' && c != '.'
            )
                return false;
        }

        return true;
    }

    protected void onInstall() {
        GameInstanceID instanceId = new GameInstanceID(txtName.getText());

        if (!checkName(instanceId.id())) {
            Controllers.dialog(new MessageDialogPane.Builder(
                    i18n("install.name.invalid"),
                    i18n("message.warning"),
                    MessageDialogPane.MessageType.QUESTION)
                    .yesOrNo(() -> {
                        controller.getSettings().put(INSTANCE_ID, instanceId);
                        controller.onFinish();
                    }, () -> {
                        // The user selects Cancel and does nothing.
                    })
                    .build());
        } else {
            controller.getSettings().put(INSTANCE_ID, instanceId);
            controller.onFinish();
        }
    }

    private void setTxtNameWithLoaders() {
        StringBuilder nameBuilder = new StringBuilder(getTitle());

        for (InstallerItem component : group.getComponents()) {
            if (component.getComponentType() == GameComponentType.GAME
                    || !controller.getSettings().containsKey(component.getComponentType().getPatchId()))
                continue;

            String loaderName = switch (component.getComponentType()) {
                case FORGE -> "Forge";
                case NEO_FORGE -> "NeoForge";
                case CLEANROOM -> "Cleanroom";
                case LEGACY_FABRIC -> "LegacyFabric";
                case FABRIC -> "Fabric";
                case LITELOADER -> "LiteLoader";
                case QUILT -> "Quilt";
                case OPTIFINE -> "OptiFine";
                default -> null;
            };

            if (loaderName != null)
                nameBuilder.append('-').append(loaderName);
        }

        txtName.setText(nameBuilder.toString());
        isNameModifiedByUser = false;
    }

    @Override
    protected boolean showExtendPane() {
        return true;
    }

    @Override
    protected void resetDefaultName() {
        isNameModifiedByUser = false;
        setTxtNameWithLoaders();
    }
}
