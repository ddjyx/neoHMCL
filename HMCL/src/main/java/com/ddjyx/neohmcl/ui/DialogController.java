// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui;

import com.ddjyx.neohmcl.auth.*;
import com.ddjyx.neohmcl.ui.account.ClassicAccountLoginDialog;
import com.ddjyx.neohmcl.ui.account.MicrosoftAccountLoginPane;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.ddjyx.neohmcl.ui.FXUtils.runInFX;

public final class DialogController {
    private DialogController() {
    }

    public static AuthInfo logIn(Account account) throws CancellationException, AuthenticationException, InterruptedException {
        if (account instanceof ClassicAccount) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<AuthInfo> res = new AtomicReference<>(null);
            runInFX(() -> {
                ClassicAccountLoginDialog pane = new ClassicAccountLoginDialog((ClassicAccount) account, it -> {
                    res.set(it);
                    latch.countDown();
                }, latch::countDown);
                Controllers.dialog(pane);
            });
            latch.await();
            return Optional.ofNullable(res.get()).orElseThrow(CancellationException::new);
        } else if (account instanceof OAuthAccount) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<AuthInfo> res = new AtomicReference<>(null);
            runInFX(() -> {
                MicrosoftAccountLoginPane pane = new MicrosoftAccountLoginPane(account, it -> {
                    res.set(it);
                    latch.countDown();
                }, latch::countDown, false);
                Controllers.dialog(pane);
            });
            latch.await();
            return Optional.ofNullable(res.get()).orElseThrow(CancellationException::new);
        }
        return account.logIn();
    }
}
