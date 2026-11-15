// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.setting;

import javafx.beans.InvalidationListener;
import org.glavo.url.WebURL;
import com.ddjyx.neohmcl.download.*;
import com.ddjyx.neohmcl.task.FetchTask;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import static com.ddjyx.neohmcl.setting.SettingsManager.settings;
import static com.ddjyx.neohmcl.task.FetchTask.DEFAULT_CONCURRENCY;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

public final class DownloadProviders {
    private DownloadProviders() {
    }

    private static final String DEFAULT_BMCLAPI_ROOT = "https://bmclapi2.bangbang93.com";
    private static final String BMCLAPI_ROOT;

    static {
        @Nullable String bmclOverride = System.getProperty("hmcl.bmclapi.override");
        if (StringUtils.isBlank(bmclOverride)) {
            BMCLAPI_ROOT = DEFAULT_BMCLAPI_ROOT;
        } else {
            WebURL url;
            try {
                url = WebURL.parseBrowserInput(bmclOverride);
            } catch (Exception e) {
                url = null;
                LOG.warning("Invalid BMCLAPI override URL: " + bmclOverride, e);
            }

            if (url == null) {
                BMCLAPI_ROOT = DEFAULT_BMCLAPI_ROOT;
            } else {
                BMCLAPI_ROOT = StringUtils.removeSuffix(url.toString(), "/");
            }
        }
    }

    private static final HMCLDownloadProvider PROVIDER = new HMCLDownloadProvider(BMCLAPI_ROOT);

    /// Initializes download provider settings and synchronizes download thread settings.
    public static void init() {
        InvalidationListener onChangeDownloadThreads = observable -> {
            FetchTask.setDownloadExecutorConcurrency(settings().autoDownloadThreadsProperty().get()
                    ? DEFAULT_CONCURRENCY
                    : settings().downloadThreadsProperty().get());
        };
        settings().autoDownloadThreadsProperty().addListener(onChangeDownloadThreads);
        settings().downloadThreadsProperty().addListener(onChangeDownloadThreads);
        onChangeDownloadThreads.invalidated(null);

        FXUtils.onChangeAndOperate(settings().versionListSourceProperty(), PROVIDER::setVersionListSource);
        FXUtils.onChangeAndOperate(settings().fileDownloadSourceProperty(), PROVIDER::setFileSource);
    }

    /**
     * Get current primary preferred download provider
     */
    public static DownloadProvider getDownloadProvider() {
        return PROVIDER;
    }

}
