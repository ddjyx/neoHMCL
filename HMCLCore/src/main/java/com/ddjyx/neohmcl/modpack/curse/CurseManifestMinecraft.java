// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.curse;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.gson.Validation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// @author huangyuhui
@JsonSerializable
public record CurseManifestMinecraft(@SerializedName("version") String gameVersion,
                                     @SerializedName("modLoaders") @Unmodifiable List<CurseManifestModLoader> modLoaders) implements Validation {

    public CurseManifestMinecraft(String gameVersion, List<@NotNull CurseManifestModLoader> modLoaders) {
        this.gameVersion = gameVersion;
        this.modLoaders = List.copyOf(modLoaders);
    }

    @Override
    public List<CurseManifestModLoader> modLoaders() {
        return modLoaders;
    }

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(gameVersion))
            throw new JsonParseException("CurseForge Manifest.gameVersion cannot be blank.");
    }

}
