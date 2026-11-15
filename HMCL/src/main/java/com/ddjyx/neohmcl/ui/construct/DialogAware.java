// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.construct;

import com.ddjyx.neohmcl.ui.Controllers;

/**
 * @author yushijinhun
 * @see Controllers#dialog(javafx.scene.layout.Region)
 */
public interface DialogAware {

    default void onDialogShown() {
    }

    default void onDialogClosed() {
    }

}
