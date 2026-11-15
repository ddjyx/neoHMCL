// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.util;

import com.ddjyx.neohmcl.upgrade.UpdateHandler;
import com.ddjyx.neohmcl.util.io.JarUtils;

import java.io.IOException;
import java.nio.file.Path;

import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

/// @author Glavo
public final class Restarter {

    /// Restart the current application.
    public static void restartSelf() throws IOException {
        LOG.info("Restarting HMCL");

        Path thisJar = JarUtils.thisJarPath();
        if (thisJar == null) {
            throw new IOException("Failed to find current HMCL location");
        }

        UpdateHandler.startJava(thisJar);
    }

    private Restarter() {
    }
}
