// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.wizard;

import javafx.scene.control.Control;
import com.ddjyx.neohmcl.util.SettingsMap;

public abstract class WizardSinglePage extends Control implements WizardPage {
    protected final Runnable onFinish;

    protected WizardSinglePage(Runnable onFinish) {
        this.onFinish = onFinish;
    }

    protected abstract Object finish();

    @Override
    public void cleanup(SettingsMap settings) {
    }
}
