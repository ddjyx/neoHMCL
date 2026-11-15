// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.game.ReleaseType;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.gson.Validation;

import java.time.Instant;

/**
 *
 * @author huangyuhui
 */
@JsonSerializable
public record GameRemoteVersionInfo(
        @SerializedName("id") String gameVersion,
        @SerializedName("time") Instant time,
        @SerializedName("releaseTime") Instant releaseTime,
        @SerializedName("type") ReleaseType type,
        @SerializedName("url") String url) implements Validation {

    @Override
    public void validate() throws JsonParseException {
        if (StringUtils.isBlank(gameVersion))
            throw new JsonParseException("GameRemoteVersion id cannot be blank");
        if (StringUtils.isBlank(url))
            throw new JsonParseException("GameRemoteVersion url cannot be blank");
    }
}
