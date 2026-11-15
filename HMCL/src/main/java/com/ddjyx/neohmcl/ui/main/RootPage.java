// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.main;

import com.jfoenix.controls.JFXPopup;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.layout.Region;
import com.ddjyx.neohmcl.game.GameInstanceID;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.game.ModpackHelper;
import com.ddjyx.neohmcl.setting.Accounts;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.terracotta.TerracottaMetadata;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.animation.AnimationUtils;
import com.ddjyx.neohmcl.ui.account.AccountAdvancedListItem;
import com.ddjyx.neohmcl.ui.account.AccountListPopupMenu;
import com.ddjyx.neohmcl.ui.construct.AdvancedListItem;
import com.ddjyx.neohmcl.ui.construct.MessageDialogPane;
import com.ddjyx.neohmcl.ui.decorator.DecoratorAnimatedPage;
import com.ddjyx.neohmcl.ui.decorator.DecoratorPage;
import com.ddjyx.neohmcl.ui.download.ModpackInstallWizardProvider;
import com.ddjyx.neohmcl.ui.instances.GameAdvancedListItem;
import com.ddjyx.neohmcl.ui.instances.GameListPopupMenu;
import com.ddjyx.neohmcl.ui.instances.Instances;
import com.ddjyx.neohmcl.ui.nbt.NBTEditorPage;
import com.ddjyx.neohmcl.ui.nbt.NBTFileType;
import com.ddjyx.neohmcl.upgrade.UpdateChecker;
import com.ddjyx.neohmcl.util.Lang;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.io.FileUtils;
import com.ddjyx.neohmcl.util.platform.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

/// Displays the home content and launcher navigation sidebar.
public class RootPage extends DecoratorAnimatedPage implements DecoratorPage {
    /// The home content, created on first access.
    private @Nullable MainPage mainPage;

    /// Creates the home page and its navigation entries.
    public RootPage() {
        getStyleClass().remove("gray-background");
        getLeft().getStyleClass().add("gray-background");

        // first item in left sidebar
        AccountAdvancedListItem accountListItem = new AccountAdvancedListItem();
        accountListItem.setOnAction(e -> Controllers.navigate(Controllers.getAccountListPage()));
        FXUtils.onSecondaryButtonClicked(accountListItem, () -> AccountListPopupMenu.show(accountListItem, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, accountListItem.getWidth(), 0));
        accountListItem.accountProperty().bind(Accounts.selectedAccountProperty());

        // second item in left sidebar
        GameAdvancedListItem gameInstanceItem = new GameAdvancedListItem();
        gameInstanceItem.setOnAction(e -> {
            @Nullable HMCLGameInstance instance = GameDirectoryManager.getSelectedRepository().getSelectedInstance();
            if (instance == null) {
                Controllers.navigate(Controllers.getGameListPage());
            } else {
                Instances.modifyGameSettings(instance);
            }
        });
        FXUtils.onScroll(gameInstanceItem, getMainPage().getInstances(), list -> {
            @Nullable HMCLGameInstance currentGame = getMainPage().getCurrentGame();
            @Nullable GameInstanceID currentId = currentGame != null ? currentGame.getId() : null;
            return Lang.indexWhere(list, instance -> instance.getId().equals(currentId));
        }, instance -> instance.getRepository().setSelectedInstance(instance));
        FXUtils.onSecondaryButtonClicked(gameInstanceItem, () -> showGameListPopupMenu(gameInstanceItem));

        if (AnimationUtils.isAnimationEnabled()) {
            gameInstanceItem.setPrepareAction(Controllers::prepareGameInstancePage);
        }

        // third item in left sidebar
        AdvancedListItem gameListItem = new AdvancedListItem();
        gameListItem.setLeftIcon(SVG.FORMAT_LIST_BULLETED);
        gameListItem.setTitle(i18n("instance.manage"));
        gameListItem.setOnAction(e -> Controllers.navigate(Controllers.getGameListPage()));
        FXUtils.onSecondaryButtonClicked(gameListItem, () -> showGameListPopupMenu(gameListItem));

        if (AnimationUtils.isAnimationEnabled()) {
            gameListItem.setPrepareAction(Controllers::prepareGameListPage);
        }

        // forth item in left sidebar
        AdvancedListItem downloadItem = new AdvancedListItem();
        downloadItem.setLeftIcon(SVG.DOWNLOAD);
        downloadItem.setTitle(i18n("download"));
        downloadItem.setOnAction(e -> Controllers.navigate(Controllers.getDownloadPage()));

        if (AnimationUtils.isAnimationEnabled()) {
            downloadItem.setPrepareAction(Controllers::prepareDownloadPage);
        }

        // fifth item in left sidebar
        AdvancedListItem launcherSettingsItem = new AdvancedListItem();
        launcherSettingsItem.setLeftIcon(SVG.SETTINGS);
        launcherSettingsItem.setTitle(i18n("settings"));
        launcherSettingsItem.setOnAction(e -> {
            Controllers.getSettingsPage().showGameSettings(GameDirectoryManager.getSelectedRepository());
            Controllers.navigate(Controllers.getSettingsPage());
        });

        if (AnimationUtils.isAnimationEnabled()) {
            launcherSettingsItem.setPrepareAction(Controllers::prepareSettingsPage);
        }

        // sixth item in left sidebar
        AdvancedListItem terracottaItem = new AdvancedListItem();
        terracottaItem.setLeftIcon(SVG.GRAPH2);
        terracottaItem.setTitle(i18n("terracotta"));
        terracottaItem.setOnAction(e -> {
            if (TerracottaMetadata.PROVIDER != null) {
                Controllers.navigate(Controllers.getTerracottaPage());
            } else {
                String message;
                if (Architecture.SYSTEM_ARCH.getBits() == Bits.BIT_32)
                    message = i18n("terracotta.unsupported.arch.32bit");
                else if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS
                        && !OperatingSystem.SYSTEM_VERSION.isAtLeast(OSVersion.WINDOWS_10))
                    message = i18n("terracotta.unsupported.os.windows.old");
                else if (Platform.SYSTEM_PLATFORM.equals(OperatingSystem.LINUX, Architecture.LOONGARCH64_OW))
                    message = i18n("terracotta.unsupported.arch.loongarch64_ow");
                else
                    message = i18n("terracotta.unsupported");

                Controllers.dialog(message, null, MessageDialogPane.MessageType.WARNING);
            }
        });

        // the left sidebar
        getSidebar()
                .startCategory(i18n("account").toUpperCase(Locale.ROOT))
                .add(accountListItem)
                .startCategory(i18n("instance").toUpperCase(Locale.ROOT))
                .add(gameInstanceItem)
                .add(gameListItem)
                .add(downloadItem)
                .startCategory(i18n("settings.launcher.general").toUpperCase(Locale.ROOT))
                .add(launcherSettingsItem)
                .add(terracottaItem)
                .addNavigationDrawerItem(i18n("contact.chat"), SVG.CHAT, () -> {
                    Controllers.getSettingsPage().showFeedback();
                    Controllers.navigate(Controllers.getSettingsPage());
                });

        // the root page, with the sidebar in left, navigator in center.
        setContent(getMainPage());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return getMainPage().stateProperty();
    }

    /// Returns the home content with repository bindings and file-drop handling installed.
    public MainPage getMainPage() {
        if (mainPage == null) {
            MainPage mainPage = new MainPage();
            FXUtils.applyDragListener(mainPage,
                    file -> ModpackHelper.isFileModpackByExtension(file) || NBTFileType.isNBTFileByExtension(file) || "json".equalsIgnoreCase(FileUtils.getExtension(file)),
                    modpacks -> {
                        Path file = modpacks.get(0);
                        if (ModpackHelper.isFileModpackByExtension(file)) {
                            Controllers.getDecorator().startWizard(
                                    new ModpackInstallWizardProvider(GameDirectoryManager.getSelectedRepository(), file),
                                    i18n("install.modpack"));
                        } else if (NBTFileType.isNBTFileByExtension(file)) {
                            try {
                                Controllers.navigate(new NBTEditorPage(file));
                            } catch (Throwable e) {
                                LOG.warning("Fail to open nbt file", e);
                                Controllers.dialog(i18n("nbt.open.failed") + "\n\n" + StringUtils.getStackTrace(e),
                                        i18n("message.error"), MessageDialogPane.MessageType.ERROR);
                            }
                        } else if ("json".equalsIgnoreCase(FileUtils.getExtension(file))) {
                            Instances.installFromJson(GameDirectoryManager.getSelectedRepository(), file);
                        }
                    });

            FXUtils.onChangeAndOperate(GameDirectoryManager.selectedInstanceProperty(), mainPage::setCurrentGame);
            mainPage.latestVersionProperty().bind(UpdateChecker.latestVersionProperty());
            this.mainPage = mainPage;
        }
        return mainPage;
    }

    /// Opens the instance selection menu beside the sidebar entry.
    private void showGameListPopupMenu(Region gameListItem) {
        GameListPopupMenu.show(gameListItem,
                JFXPopup.PopupVPosition.TOP,
                JFXPopup.PopupHPosition.LEFT,
                gameListItem.getWidth(),
                0,
                getMainPage().getInstances());
    }
}
