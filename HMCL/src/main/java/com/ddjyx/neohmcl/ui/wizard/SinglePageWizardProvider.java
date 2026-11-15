// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.wizard;

import javafx.scene.Node;
import com.ddjyx.neohmcl.util.SettingsMap;

import java.util.function.Function;

public class SinglePageWizardProvider implements WizardProvider {

    private final Function<WizardController, WizardSinglePage> provider;
    private WizardSinglePage page;

    public SinglePageWizardProvider(Function<WizardController, WizardSinglePage> provider) {
        this.provider = provider;
    }

    @Override
    public void start(SettingsMap settings) {
    }

    @Override
    public Object finish(SettingsMap settings) {
        return page.finish();
    }

    @Override
    public Node createPage(WizardController controller, int step, SettingsMap settings) {
        if (step != 0) throw new IllegalStateException("Step must be 0");

        return page = provider.apply(controller);
    }

    @Override
    public boolean cancel() {
        return true;
    }
}
