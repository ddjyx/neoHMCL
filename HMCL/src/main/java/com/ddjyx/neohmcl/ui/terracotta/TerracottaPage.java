// SPDX-FileCopyrightText: Copyright (C) 2025 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.terracotta;

import com.jfoenix.controls.JFXPopup;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import com.ddjyx.neohmcl.game.GameInstanceID;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.setting.*;
import com.ddjyx.neohmcl.terracotta.TerracottaMetadata;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.account.AccountAdvancedListItem;
import com.ddjyx.neohmcl.ui.account.AccountListPopupMenu;
import com.ddjyx.neohmcl.ui.animation.TransitionPane;
import com.ddjyx.neohmcl.ui.construct.*;
import com.ddjyx.neohmcl.ui.decorator.DecoratorAnimatedPage;
import com.ddjyx.neohmcl.ui.decorator.DecoratorPage;
import com.ddjyx.neohmcl.ui.main.MainPage;
import com.ddjyx.neohmcl.ui.instances.GameListPopupMenu;
import com.ddjyx.neohmcl.ui.instances.Instances;
import com.ddjyx.neohmcl.util.Lang;
import org.jetbrains.annotations.Nullable;

import static com.ddjyx.neohmcl.setting.SettingsManager.userState;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public class TerracottaPage extends DecoratorAnimatedPage implements DecoratorPage, PageAware {
    private static final int TERRACOTTA_AGREEMENT_VERSION = 2;

    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(i18n("terracotta.terracotta")));
    /// Manages tab selection and content transitions independently of the sidebar.
    private final TabController tabController;
    /// The tab displaying the Terracotta controller page.
    private final TabItem<TerracottaControllerPage> statusPage;
    private final TransitionPane transitionPane = new TransitionPane();

    @SuppressWarnings("unused")
    private @Nullable ChangeListener<@Nullable HMCLGameInstance> instanceChangeListenerHolder;

    /// Creates the connection tab and the fixed account and launch actions.
    public TerracottaPage() {
        statusPage = new TabItem<>(TerracottaControllerPage::new);
        tabController = new TabController(transitionPane, statusPage);
        tabController.selectTab(statusPage);

        getSidebar()
                .addNavigationDrawerTab(tabController, statusPage, i18n("terracotta.status"), SVG.TUNE);

        AccountAdvancedListItem accountListItem = new AccountAdvancedListItem();
        accountListItem.setOnAction(e -> Controllers.navigate(Controllers.getAccountListPage()));
        accountListItem.accountProperty().bind(Accounts.selectedAccountProperty());
        FXUtils.onSecondaryButtonClicked(accountListItem, () -> AccountListPopupMenu.show(accountListItem, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.LEFT, accountListItem.getWidth(), 0));

        AdvancedListBox toolbar = new AdvancedListBox()
                .add(accountListItem)
                .addNavigationDrawerItem(i18n("instance.launch"), SVG.ROCKET_LAUNCH, () -> {
                    var repository = GameDirectoryManager.getSelectedRepository();
                    Instances.launch(repository.getSelectedInstance(), launcherHelper -> {
                        launcherHelper.setKeep();
                        launcherHelper.setDisableOfflineSkin();
                    });
                }, item -> {
                    instanceChangeListenerHolder = FXUtils.onWeakChangeAndOperate(GameDirectoryManager.selectedInstanceProperty(),
                            instance -> item.setSubtitle(instance != null ? instance.getId().toString() : i18n("instance.empty"))
                    );

                    MainPage mainPage = Controllers.getRootPage().getMainPage();
                    FXUtils.onScroll(item, mainPage.getInstances(), list -> {
                        @Nullable HMCLGameInstance currentGame = mainPage.getCurrentGame();
                        @Nullable GameInstanceID currentId = currentGame != null ? currentGame.getId() : null;
                        return Lang.indexWhere(list, instance -> instance.getId().equals(currentId));
                    }, instance -> instance.getRepository().setSelectedInstance(instance));

                    FXUtils.onSecondaryButtonClicked(item, () -> GameListPopupMenu.show(item,
                            JFXPopup.PopupVPosition.BOTTOM,
                            JFXPopup.PopupHPosition.LEFT,
                            item.getWidth(),
                            0,
                            mainPage.getInstances()));
                })
                .addNavigationDrawerItem(i18n("terracotta.feedback.title"), SVG.FEEDBACK, () -> FXUtils.openLink(TerracottaMetadata.FEEDBACK_LINK));
        setSidebarBottom(toolbar);

        setContent(transitionPane);
    }

    @Override
    public void onPageShown() {
        tabController.onPageShown();

        if (SettingsManager.userState().terracottaAgreementVersionProperty().get() < TERRACOTTA_AGREEMENT_VERSION) {
            Controllers.confirmWithCountdown(i18n("terracotta.confirm.desc"), i18n("terracotta.confirm.title"), 5, MessageDialogPane.MessageType.INFO, () -> {
                UserState userState = userState();
                userState.terracottaAgreementVersionProperty().set(TERRACOTTA_AGREEMENT_VERSION);
            }, () -> fireEvent(new PageCloseEvent()));
        }
    }

    @Override
    public void onPageHidden() {
        tabController.onPageHidden();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }
}
