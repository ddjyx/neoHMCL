// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.directory;

import com.jfoenix.controls.JFXButton;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.SkinBase;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.ui.SVGContainer;
import com.ddjyx.neohmcl.ui.animation.Motion;
import com.ddjyx.neohmcl.ui.construct.RipplerContainer;
import com.ddjyx.neohmcl.ui.construct.TwoLineListItem;
import org.jetbrains.annotations.NotNullByDefault;

/// Skin for [GameDirectoryListItem].
@NotNullByDefault
public class GameDirectoryListItemSkin extends SkinBase<GameDirectoryListItem> {
    /// Pseudo class applied to the selected item.
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    /// Creates the skin for a game directory list item.
    ///
    /// @param skinnable the list item controlled by this skin
    public GameDirectoryListItemSkin(GameDirectoryListItem skinnable) {
        super(skinnable);

        TwoLineListItem item = new TwoLineListItem();
        RipplerContainer container = new RipplerContainer(item);

        SVGContainer left = (skinnable.isSelected() ? SVG.FOLDER_FILL : SVG.FOLDER).createIcon(20);
        left.setMouseTransparent(true);
        TwoLineListItem.setMargin(left, new Insets(0, 6, 0, 6));
        item.setLeading(left);

        FXUtils.onChangeAndOperate(skinnable.selectedProperty(), active -> {
            skinnable.pseudoClassStateChanged(SELECTED, active);

            SVG targetIcon = active ? SVG.FOLDER_FILL : SVG.FOLDER;
            if (left.getIcon() != targetIcon) {
                left.setIcon(targetIcon, Motion.SHORT4);
            }
        });

        FXUtils.onClicked(getSkinnable(), () -> GameDirectoryManager.setSelectedGameDirectory(skinnable.getGameDirectory()));

        JFXButton btnRemove = FXUtils.newToggleButton4(SVG.CLOSE, 14);
        btnRemove.setOnAction(e -> skinnable.remove());
        item.setTrailing(btnRemove);

        item.titleProperty().set(GameDirectoryManager.getGameDirectoryDisplayName(skinnable.getGameDirectory()));
        item.subtitleProperty().set(skinnable.getGameDirectory().getPath().toString());

        getChildren().setAll(container);
    }
}
