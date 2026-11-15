// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.util.io;

import java.io.IOException;

public class NoCandidatesException extends IOException {

    public NoCandidatesException() {
        super("No candidates found");
    }
}
