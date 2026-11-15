// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.auth.yggdrasil;

public enum TextureModel {
    WIDE("default"), SLIM("slim");

    public final String modelName;

    TextureModel(String modelName) {
        this.modelName = modelName;
    }
}
