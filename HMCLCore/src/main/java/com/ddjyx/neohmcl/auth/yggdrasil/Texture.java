// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.auth.yggdrasil;

import com.ddjyx.neohmcl.util.Immutable;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Immutable
public record Texture(String url, Map<String, String> metadata) {

    public Texture() {
        this(null, null);
    }

    @Override
    @Nullable
    public String url() {
        return url;
    }

    @Override
    @Nullable
    public Map<String, String> metadata() {
        return metadata;
    }
}
