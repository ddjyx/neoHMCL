// SPDX-FileCopyrightText: Copyright (C) 2025 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.terracotta.provider;

import com.ddjyx.neohmcl.terracotta.TerracottaBundle;

import java.nio.file.Path;
import java.util.List;

public final class GeneralProvider extends AbstractTerracottaProvider {
    private final Path executable;

    public GeneralProvider(TerracottaBundle bundle, Path executable) {
        super(bundle);
        this.executable = executable;
    }

    @Override
    public List<String> ofCommandLine(Path portTransfer) {
        return List.of(executable.toString(), "--hmcl", portTransfer.toString());
    }
}
