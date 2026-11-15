// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.Immutable;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.TolerableValidationException;

/**
 *
 * @author huangyuhui
 */
@Immutable
public class IdDownloadInfo extends DownloadInfo {

    @SerializedName("id")
    private final String id;

    public IdDownloadInfo() {
        this("", "");
    }

    public IdDownloadInfo(String id, String url) {
        this(id, url, null);
    }

    public IdDownloadInfo(String id, String url, String sha1) {
        this(id, url, sha1, 0);
    }

    public IdDownloadInfo(String id, String url, String sha1, int size) {
        super(url, sha1, size);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        super.validate();

        if (StringUtils.isBlank(id))
            throw new JsonParseException("IdDownloadInfo id can not be null");
    }

}
