// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.setting.property;

import javafx.beans.property.Property;
import com.ddjyx.neohmcl.setting.GameSettings;
import com.ddjyx.neohmcl.util.gson.RawPreservingProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.UnknownNullability;

/// @author Glavo
@NotNullByDefault
public interface SettingProperty<T extends @UnknownNullability Object> extends Property<T>, RawPreservingProperty<T> {
    @Override
    GameSettings getBean();

    T defaultValue();
}
