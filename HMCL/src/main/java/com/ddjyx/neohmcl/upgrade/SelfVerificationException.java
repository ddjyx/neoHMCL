// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.upgrade;

import java.io.IOException;

public class SelfVerificationException extends IOException {

    public SelfVerificationException() {
        super("Self verification failed");
    }

}
