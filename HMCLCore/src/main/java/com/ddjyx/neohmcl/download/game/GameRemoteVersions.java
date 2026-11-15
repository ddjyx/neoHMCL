// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.Immutable;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.gson.Validation;

import java.util.List;

/**
 *
 * @author huangyuhui
 */
@Immutable
@JsonSerializable
public record GameRemoteVersions(
        @SerializedName("versions") List<GameRemoteVersionInfo> versions,
        @SerializedName("latest") GameRemoteLatestVersions latest) implements Validation {

    @Override
    public void validate() throws JsonParseException {
        if (versions == null)
            throw new JsonParseException("GameRemoteVersions.versions cannot be null");
    }
}
