// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.ddjyx.neohmcl.auth.Account;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.game.TexturesLoader;
import com.ddjyx.neohmcl.setting.Accounts;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.account.AccountAdvancedListItem;
import com.ddjyx.neohmcl.ui.construct.IconedMenuItem;
import com.ddjyx.neohmcl.ui.construct.PopupMenu;
import com.ddjyx.neohmcl.ui.decorator.DecoratorAnimatedPage;
import com.ddjyx.neohmcl.ui.decorator.DecoratorPage;
import com.ddjyx.neohmcl.ui.instances.Instances;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

/// Presents the shared home content with PCL-inspired navigation and account controls.
///
/// This page owns presentation only. Game launching, account selection, and all destination
/// pages remain the existing HMCL implementations shared with the classic home page.
@NotNullByDefault
public final class NeoHmclRootPage extends DecoratorAnimatedPage implements DecoratorPage {
    /// The title-bar state containing the neoHMCL navigation.
    private final ReadOnlyObjectWrapper<State> state;

    /// The shared home content whose launch behavior is reused.
    private final MainPage mainPage;

    /// Creates the neoHMCL home around the same content used by the classic home.
    ///
    /// @param mainPage the shared home content with live instance and update bindings
    public NeoHmclRootPage(MainPage mainPage) {
        this.mainPage = mainPage;
        mainPage.setNeoHmclLayout(true);
        getStyleClass().remove("gray-background");
        getStyleClass().add("neohmcl-root");
        getLeft().getStyleClass().remove("gray-background");

        HBox navigation = new HBox(8);
        navigation.setAlignment(Pos.CENTER_LEFT);
        Label wordmark = new Label("neoHMCL");
        wordmark.getStyleClass().add("neohmcl-wordmark");
        navigation.getChildren().add(wordmark);
        navigation.getChildren().add(navigationButton(i18n("instance.launch"), SVG.ROCKET_LAUNCH,
                Controllers::navigateHome));
        navigation.getChildren().add(navigationButton(i18n("download"), SVG.DOWNLOAD,
                () -> Controllers.navigate(Controllers.getDownloadPage())));
        navigation.getChildren().add(navigationButton(i18n("settings"), SVG.SETTINGS,
                NeoHmclRootPage::openSettings));
        JFXButton moreButton = navigationButton(i18n("neohmcl.more"), SVG.MENU, () -> { });
        moreButton.setOnAction(event -> showMoreMenu(moreButton));
        navigation.getChildren().add(moreButton);
        state = new ReadOnlyObjectWrapper<>(new State(null, navigation, false, false, true));

        BorderPane accountPane = (BorderPane) getLeft();
        accountPane.getStyleClass().add("neohmcl-account-pane");
        accountPane.setPadding(new Insets(24));
        accountPane.setPrefWidth(320);
        accountPane.setMinWidth(280);
        accountPane.setMaxWidth(360);
        setContent(mainPage);
        getContent().getStyleClass().add("neohmcl-workspace");
        buildAccountPane();
    }

    /// Builds live account and instance controls without duplicating their business logic.
    private void buildAccountPane() {
        AccountAdvancedListItem accountItem = new AccountAdvancedListItem();
        accountItem.accountProperty().bind(Accounts.selectedAccountProperty());
        accountItem.setOnAction(event -> Controllers.navigate(Controllers.getAccountListPage()));
        accountItem.getStyleClass().add("neohmcl-account-type");

        HBox accountTypes = new HBox(12);
        accountTypes.setAlignment(Pos.CENTER);
        accountTypes.getChildren().add(accountItem);
        accountItem.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(accountItem, Priority.ALWAYS);

        Canvas avatar = new Canvas(72, 72);
        Label profileName = new Label();
        profileName.getStyleClass().add("neohmcl-profile-name");
        profileName.setWrapText(true);
        profileName.setAlignment(Pos.CENTER);
        profileName.setMaxWidth(Double.MAX_VALUE);

        FXUtils.onChangeAndOperate(Accounts.selectedAccountProperty(), account -> {
            TexturesLoader.unbindAvatar(avatar);
            if (account == null) {
                TexturesLoader.drawAvatar(avatar, TexturesLoader.getDefaultSkinImage());
            } else {
                TexturesLoader.bindAvatar(avatar, account);
            }
        });
        profileName.textProperty().bind(Bindings.createStringBinding(() -> {
            Account account = Accounts.getSelectedAccount();
            if (account == null) {
                return i18n("account.missing");
            }
            String name = account.getProfileName();
            return name == null || name.isBlank() ? account.getProfileID().toString() : name;
        }, Accounts.selectedAccountProperty()));

        VBox profile = new VBox(16, avatar, profileName);
        profile.setAlignment(Pos.CENTER);
        VBox.setMargin(profile, new Insets(56, 0, 0, 0));

        HBox instanceActions = new HBox(8);
        instanceActions.setAlignment(Pos.CENTER);
        JFXButton chooseVersion = outlineButton(i18n("instance.manage"), () ->
                Controllers.navigate(Controllers.getGameListPage()));
        JFXButton editVersion = outlineButton(i18n("instance.settings"), NeoHmclRootPage::editSelectedInstance);
        instanceActions.getChildren().setAll(chooseVersion, editVersion);
        HBox.setHgrow(chooseVersion, Priority.ALWAYS);
        HBox.setHgrow(editVersion, Priority.ALWAYS);

        JFXButton launch = outlineButton("", mainPage::launchOrInstall);
        launch.getStyleClass().add("neohmcl-launch-button");
        launch.setMaxWidth(Double.MAX_VALUE);

        Label launchTitle = new Label();
        launchTitle.getStyleClass().add("neohmcl-launch-title");
        Label instanceName = new Label();
        instanceName.getStyleClass().add("neohmcl-instance-name");
        VBox launchText = new VBox(4, launchTitle, instanceName);
        launchText.setAlignment(Pos.CENTER);
        launchText.setMouseTransparent(true);
        launch.setGraphic(launchText);

        FXUtils.onChangeAndOperate(GameDirectoryManager.selectedInstanceProperty(), instance -> {
            launchTitle.setText(i18n("instance.launch"));
            instanceName.setText(instance == null ? i18n("instance.empty") : instance.getId().toString());
            editVersion.setDisable(instance == null);
        });

        VBox actions = new VBox(8, launch, instanceActions);
        actions.setFillWidth(true);
        ((BorderPane) getLeft()).setTop(accountTypes);
        ((BorderPane) getLeft()).setCenter(profile);
        ((BorderPane) getLeft()).setBottom(actions);
    }

    /// Creates a title-bar navigation button using the shared navigation actions.
    private JFXButton navigationButton(String title, SVG icon, Runnable action) {
        JFXButton button = new JFXButton();
        button.setFocusTraversable(false);
        button.setGraphicTextGap(8);
        button.setGraphic(icon.createIcon(20));
        button.setText(title);
        button.getStyleClass().add("neohmcl-nav-button");
        button.setOnAction(event -> action.run());
        return button;
    }

    /// Creates a neoHMCL outline button that fills its allocated width.
    private static JFXButton outlineButton(String title, Runnable action) {
        JFXButton button = new JFXButton(title);
        button.getStyleClass().add("neohmcl-outline-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    /// Opens the existing launcher settings page.
    private static void openSettings() {
        Controllers.getSettingsPage().showGameSettings(GameDirectoryManager.getSelectedRepository());
        Controllers.navigate(Controllers.getSettingsPage());
    }

    /// Opens settings for the selected instance, or the instance list when none exists.
    private static void editSelectedInstance() {
        @Nullable HMCLGameInstance instance = GameDirectoryManager.getSelectedRepository().getSelectedInstance();
        if (instance == null) {
            Controllers.navigate(Controllers.getGameListPage());
        } else {
            Instances.modifyGameSettings(instance);
        }
    }

    /// Shows existing destinations that do not belong in the primary navigation.
    private void showMoreMenu(Node owner) {
        PopupMenu menu = new PopupMenu();
        JFXPopup popup = new JFXPopup(menu);
        menu.getContent().add(new IconedMenuItem(SVG.PERSON, i18n("account"),
                () -> Controllers.navigate(Controllers.getAccountListPage()), popup));
        menu.getContent().add(new IconedMenuItem(SVG.FORMAT_LIST_BULLETED, i18n("instance.manage"),
                () -> Controllers.navigate(Controllers.getGameListPage()), popup));
        menu.getContent().add(new IconedMenuItem(SVG.STYLE, i18n("settings.launcher.appearance"),
                () -> {
                    Controllers.getSettingsPage().showPersonalization();
                    Controllers.navigate(Controllers.getSettingsPage());
                }, popup));
        popup.show(owner, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.LEFT, 0, 0);
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }
}
