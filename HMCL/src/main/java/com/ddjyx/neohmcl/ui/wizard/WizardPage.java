// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.wizard;

import com.ddjyx.neohmcl.util.SettingsMap;

public interface WizardPage {
    default void onNavigate(SettingsMap settings) {
    }

    default void cleanup(SettingsMap settings) {
    }

    String getTitle();
}
