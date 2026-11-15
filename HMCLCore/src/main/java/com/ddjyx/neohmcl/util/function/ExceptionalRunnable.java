// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.util.function;

import java.util.concurrent.Callable;

/**
 *
 * @author huangyuhui
 */
public interface ExceptionalRunnable<E extends Exception> {

    void run() throws E;

    default Callable<Void> toCallable() {
        return () -> {
            run();
            return null;
        };
    }

}
