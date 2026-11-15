// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.construct;

import javafx.scene.control.Hyperlink;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;

public final class JFXHyperlink extends Hyperlink {

    public JFXHyperlink(String text) {
        super(text);

        setGraphic(SVG.OPEN_IN_NEW.createIcon(16));
    }

    public void setExternalLink(String externalLink) {
        this.setOnAction(e -> FXUtils.openLink(externalLink));
    }
}

