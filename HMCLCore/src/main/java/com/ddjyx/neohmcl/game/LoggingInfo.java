// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.game;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.gson.TolerableValidationException;
import com.ddjyx.neohmcl.util.gson.Validation;

/**
 *
 * @author huangyuhui
 */
public record LoggingInfo(@SerializedName("file") IdDownloadInfo file, @SerializedName("argument") String argument,
                          @SerializedName("type") String type) implements Validation {

    public LoggingInfo() {
        this(new IdDownloadInfo());
    }

    public LoggingInfo(IdDownloadInfo file) {
        this(file, "");
    }

    public LoggingInfo(IdDownloadInfo file, String argument) {
        this(file, argument, "");
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        file.validate();
        if (StringUtils.isBlank(argument))
            throw new JsonParseException("LoggingInfo.argument is empty.");
        if (StringUtils.isBlank(type))
            throw new JsonParseException("LoggingInfo.type is empty.");
    }
}
