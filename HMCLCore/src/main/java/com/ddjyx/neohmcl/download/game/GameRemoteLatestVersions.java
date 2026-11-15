// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.game;

import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.Immutable;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;

/**
 * @author huangyuhui
 */
@Immutable
@JsonSerializable
public record GameRemoteLatestVersions(
        @SerializedName("snapshot") String snapshot,
        @SerializedName("release") String release) {
}
