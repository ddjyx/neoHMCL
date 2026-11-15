// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.account;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import com.ddjyx.neohmcl.auth.Account;
import com.ddjyx.neohmcl.auth.AuthenticationException;
import com.ddjyx.neohmcl.auth.ClassicAccount;
import com.ddjyx.neohmcl.auth.CredentialExpiredException;
import com.ddjyx.neohmcl.auth.authlibinjector.AuthlibInjectorAccount;
import com.ddjyx.neohmcl.auth.authlibinjector.AuthlibInjectorServer;
import com.ddjyx.neohmcl.auth.offline.OfflineAccount;
import com.ddjyx.neohmcl.auth.yggdrasil.CompleteGameProfile;
import com.ddjyx.neohmcl.auth.yggdrasil.TextureType;
import com.ddjyx.neohmcl.setting.Accounts;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.DialogController;
import com.ddjyx.neohmcl.ui.construct.MessageDialogPane.MessageType;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.skin.InvalidSkinException;
import com.ddjyx.neohmcl.util.skin.NormalizedSkin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;

import static java.util.Collections.emptySet;
import static javafx.beans.binding.Bindings.createBooleanBinding;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

public class AccountListItem extends RadioButton {

    private final Account account;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();

    public AccountListItem(Account account) {
        this.account = account;
        getStyleClass().clear();
        setUserData(account);

        String loginTypeName = Accounts.getLocalizedLoginTypeName(Accounts.getAccountFactory(account));
        String portableSuffix = account.isPortable() ? ", " + i18n("account.portable") : "";
        if (account instanceof AuthlibInjectorAccount) {
            AuthlibInjectorServer server = ((AuthlibInjectorAccount) account).getServer();
            subtitle.bind(Bindings.concat(
                    loginTypeName, ", ", i18n("account.injector.server"), ": ",
                    Bindings.createStringBinding(server::getName, server), portableSuffix));
        } else {
            subtitle.set(loginTypeName + portableSuffix);
        }

        StringBinding profileName = Bindings.createStringBinding(() -> {
            String name = account.getProfileName();
            return StringUtils.isBlank(name) ? account.getProfileID().toString() : name;
        }, account);
        if (account instanceof ClassicAccount classicAccount) {
            title.bind(Bindings.concat(profileName, " - ", classicAccount.getLoginName()));
        } else {
            title.bind(profileName);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AccountListItemSkin(this);
    }

    public Task<?> refreshAsync() {
        return Task.runAsync(() -> {
            account.clearCache();
            try {
                account.logIn();
            } catch (CredentialExpiredException e) {
                try {
                    DialogController.logIn(account);
                } catch (CancellationException e1) {
                    // ignore cancellation
                } catch (Exception e1) {
                    LOG.warning("Failed to refresh " + account + " with password", e1);
                    throw e1;
                }
            } catch (AuthenticationException e) {
                LOG.warning("Failed to refresh " + account + " with token", e);
                throw e;
            }
        });
    }

    public ObservableBooleanValue canUploadSkin() {
        if (account instanceof AuthlibInjectorAccount aiAccount) {
            ObjectBinding<Optional<CompleteGameProfile>> profile = aiAccount.getYggdrasilService().getProfileRepository().binding(aiAccount.getProfileID());
            return createBooleanBinding(() -> {
                Set<TextureType> uploadableTextures = profile.get()
                        .map(AuthlibInjectorAccount::getUploadableTextures)
                        .orElse(emptySet());
                return uploadableTextures.contains(TextureType.SKIN);
            }, profile);
        } else if (account instanceof OfflineAccount || account.canUploadSkin()) {
            return createBooleanBinding(() -> true);
        } else {
            return createBooleanBinding(() -> false);
        }
    }

    /**
     * @return the skin upload task, null if no file is selected
     */
    @Nullable
    public Task<?> uploadSkin() {
        if (account instanceof OfflineAccount) {
            Controllers.dialog(new OfflineAccountSkinPane((OfflineAccount) account));
            return null;
        }
        if (!account.canUploadSkin()) {
            return null;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("account.skin.upload"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(i18n("account.skin.file"), "*.png"));
        Path selectedFile = Controllers.showOpenDialog(chooser);
        if (selectedFile == null) {
            return null;
        }

        return refreshAsync()
                .thenRunAsync(() -> {
                    Image skinImg;
                    try (var input = Files.newInputStream(selectedFile)) {
                        skinImg = new Image(input);
                    } catch (IOException e) {
                        throw new InvalidSkinException("Failed to read skin image", e);
                    }
                    if (skinImg.isError()) {
                        throw new InvalidSkinException("Failed to read skin image", skinImg.getException());
                    }
                    if (skinImg.getWidth() != 64 || (skinImg.getHeight() != 32 && skinImg.getHeight() != 64)) {
                        throw new InvalidSkinException("Invalid skin size");
                    }
                    NormalizedSkin skin = new NormalizedSkin(skinImg);
                    String model = skin.isSlim() ? "slim" : "";
                    LOG.info("Uploading skin [" + selectedFile + "], model [" + model + "]");
                    account.uploadSkin(skin.isSlim(), selectedFile);
                })
                .thenComposeAsync(refreshAsync())
                .whenComplete(Schedulers.javafx(), e -> {
                    if (e != null) {
                        Controllers.dialog(Accounts.localizeErrorMessage(e), i18n("account.skin.upload.failed"), MessageType.ERROR);
                    }
                });
    }

    public void remove() {
        if (!Accounts.canRemoveAccount(account)) {
            Controllers.confirmBackupAndOverwrite(i18n("account.storage.read_only"), () -> {
                Accounts.forceOverwriteAccountFiles(account);
                Accounts.getAccounts().remove(account);
            });
            return;
        }

        Accounts.getAccounts().remove(account);
    }

    public Account getAccount() {
        return account;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getSubtitle() {
        return subtitle.get();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.set(subtitle);
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }
}
