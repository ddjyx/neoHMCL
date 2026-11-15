// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.util.function;

public interface ExceptionalPredicate<T, E extends Exception> {
    boolean test(T t) throws E;
}
