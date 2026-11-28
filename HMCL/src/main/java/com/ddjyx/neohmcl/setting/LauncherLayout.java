// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.setting;

import org.jetbrains.annotations.NotNullByDefault;

/// Selects the launcher home layout independently of the active color theme.
@NotNullByDefault
public enum LauncherLayout {
    /// Uses the classic HMCL navigation sidebar and home content.
    HMCL,

    /// Uses the PCL-inspired top navigation and account sidebar.
    ///
    /// The enum constant name is preserved for backward compatibility with previously saved settings.
    PCL
}
