// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.account;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tooltip;
import com.ddjyx.neohmcl.auth.Account;
import com.ddjyx.neohmcl.auth.authlibinjector.AuthlibInjectorAccount;
import com.ddjyx.neohmcl.auth.authlibinjector.AuthlibInjectorServer;
import com.ddjyx.neohmcl.game.TexturesLoader;
import com.ddjyx.neohmcl.setting.Accounts;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.construct.AdvancedListItem;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.javafx.BindingMapping;
import org.jetbrains.annotations.Nullable;

import static javafx.beans.binding.Bindings.createStringBinding;
import static com.ddjyx.neohmcl.setting.Accounts.getAccountFactory;
import static com.ddjyx.neohmcl.setting.Accounts.getLocalizedLoginTypeName;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public class AccountAdvancedListItem extends AdvancedListItem {
    private final Tooltip tooltip;
    private final Canvas canvas;
    private boolean tooltipInstalled;

    private final ObjectProperty<Account> account = new SimpleObjectProperty<Account>() {

        @Override
        protected void invalidated() {
            Account account = get();
            if (account == null) {
                titleProperty().unbind();
                subtitleProperty().unbind();
                setTitle(i18n("account.missing"));
                setSubtitle(i18n("account.missing.add"));
                tooltip.setText(i18n("account.create"));
                installTooltip();

                TexturesLoader.unbindAvatar(canvas);
                TexturesLoader.drawAvatar(canvas, TexturesLoader.getDefaultSkinImage());

            } else {
                titleProperty().bind(createStringBinding(() -> {
                    String profileName = account.getProfileName();
                    return StringUtils.isBlank(profileName) ? account.getProfileID().toString() : profileName;
                }, account));
                subtitleProperty().bind(accountSubtitle(account));
                uninstallTooltip();
                TexturesLoader.bindAvatar(canvas, account);
            }
        }
    };

    public AccountAdvancedListItem() {
        this(null);
    }

    /// Creates an account item; null enables scrolling through the configured accounts.
    public AccountAdvancedListItem(@Nullable Account account) {
        tooltip = new Tooltip();

        canvas = new Canvas(32, 32);
        canvas.setMouseTransparent(true);

        setLeftGraphic(canvas);

        if (account != null) {
            this.accountProperty().set(account);
        } else {
            FXUtils.onScroll(this, Accounts.getAccounts(),
                    accounts -> accounts.indexOf(accountProperty().get()),
                    Accounts::setSelectedAccount);
        }
    }

    public ObjectProperty<Account> accountProperty() {
        return account;
    }

    private static ObservableValue<String> accountSubtitle(Account account) {
        if (account instanceof AuthlibInjectorAccount) {
            return BindingMapping.of(((AuthlibInjectorAccount) account).getServer(), AuthlibInjectorServer::getName);
        } else {
            return createStringBinding(() -> getLocalizedLoginTypeName(getAccountFactory(account)));
        }
    }

    private void installTooltip() {
        if (!tooltipInstalled) {
            FXUtils.installFastTooltip(this, tooltip);
            tooltipInstalled = true;
        }
    }

    private void uninstallTooltip() {
        if (tooltipInstalled) {
            Tooltip.uninstall(this, tooltip);
            tooltipInstalled = false;
        }
    }

}
