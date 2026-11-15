// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.addon.update;

import com.ddjyx.neohmcl.addon.LocalAddonFile;
import com.ddjyx.neohmcl.addon.RemoteAddon;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public record AddonUpdate(
        LocalAddonFile<?> localAddonFile,
        RemoteAddon.Version currentVersion,
        RemoteAddon.Version targetVersion
) {
}
