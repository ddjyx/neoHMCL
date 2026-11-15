// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.download;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import com.ddjyx.neohmcl.download.ComponentRemoteVersion;
import com.ddjyx.neohmcl.download.DownloadProvider;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.ui.InstallerItem;
import com.ddjyx.neohmcl.ui.wizard.WizardController;
import com.ddjyx.neohmcl.util.Lang;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;

import java.util.Optional;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

class AdditionalInstallersPage extends AbstractInstallersPage {
    protected final BooleanProperty compatible = new SimpleBooleanProperty();
    protected final GameVersionNumber gameVersion;
    protected final GameInstanceManifest manifest;
    protected final HMCLGameInstance instance;

    public AdditionalInstallersPage(HMCLGameInstance instance, GameVersionNumber gameVersion, WizardController controller, DownloadProvider downloadProvider) {
        super(controller, gameVersion, downloadProvider);
        this.instance = instance;
        this.gameVersion = gameVersion;
        this.manifest = instance.getManifest();

        txtName.setText(instance.getId().id());
        txtName.setEditable(false);

        for (InstallerItem component : group.getComponents()) {
            if (component.getComponentType() == GameComponentType.GAME) continue;
            component.setOnRemove(() -> {
                controller.getSettings().put(
                        component.getComponentType().getPatchId(),
                        new UpdateInstallerWizardProvider.RemoveComponentAction(component.getComponentType()));
                reload();
            });
        }

        installable.bind(Bindings.createBooleanBinding(() -> txtName.validate(), txtName.textProperty()));
    }

    @Override
    protected void onInstall() {
        controller.onFinish();
    }

    @Override
    public String getTitle() {
        return i18n("install.change_version.title", instance.getId().id());
    }

    private String getVersion(GameComponentType type) {
        return Optional.ofNullable(controller.getSettings().get(type.getPatchId()))
                .flatMap(it -> Lang.tryCast(it, ComponentRemoteVersion.class))
                .map(ComponentRemoteVersion::getSelfVersion).orElse(null);
    }

    @Override
    protected void reload() {
        for (InstallerItem component : group.getComponents()) {
            GameComponentType componentType = component.getComponentType();
            if (controller.getSettings().containsKey(componentType.getPatchId())) {
                component.versionProperty().set(new InstallerItem.InstalledState(getVersion(componentType), false, false));
            } else {
                component.versionProperty().set(null);
            }
        }
    }

    @Override
    public void cleanup(SettingsMap settings) {
    }

    @Override
    protected boolean showExtendPane() {
        return false;
    }

    @Override
    protected void resetDefaultName() {
    }
}
