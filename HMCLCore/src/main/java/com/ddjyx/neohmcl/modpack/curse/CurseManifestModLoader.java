// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.curse;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.Immutable;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.Validation;

/**
 *
 * @author huangyuhui
 */
@Immutable
public record CurseManifestModLoader(@SerializedName("id") String id,
                                     @SerializedName("primary") boolean primary) implements Validation {
    public CurseManifestModLoader() {
        this("", false);
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(id))
            throw new JsonParseException("Curse Forge modpack manifest Mod loader id cannot be blank.");
    }

}
