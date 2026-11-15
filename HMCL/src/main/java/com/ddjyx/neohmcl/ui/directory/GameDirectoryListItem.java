// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.directory;

import javafx.beans.binding.Bindings;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Skin;

import com.ddjyx.neohmcl.setting.GameDirectory;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.ui.Controllers;
import org.jetbrains.annotations.NotNullByDefault;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

/// Navigation drawer item for one game directory entry.
@NotNullByDefault
public class GameDirectoryListItem extends RadioButton {
    /// Game directory represented by this item.
    private final GameDirectory gameDirectory;

    /// Creates a list item for the given game directory.
    ///
    /// @param gameDirectory the represented game directory
    public GameDirectoryListItem(GameDirectory gameDirectory) {
        this.gameDirectory = gameDirectory;
        getStyleClass().setAll("game-directory-list-item", "navigation-drawer-item");
        setUserData(gameDirectory);

        this.selectedProperty().bind(Bindings.equal(gameDirectory, GameDirectoryManager.selectedGameDirectoryProperty()));
    }

    /// Creates the JavaFX skin for this item.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new GameDirectoryListItemSkin(this);
    }

    /// Removes the represented game directory after handling read-only storage.
    public void remove() {
        if (!GameDirectoryManager.canRemoveGameDirectory(gameDirectory)) {
            Controllers.confirmBackupAndOverwrite(i18n("settings.game_directories.read_only"), () -> {
                GameDirectoryManager.forceOverwriteGameDirectoryFiles(gameDirectory);
                GameDirectoryManager.removeGameDirectory(gameDirectory);
            });
            return;
        }

        GameDirectoryManager.removeGameDirectory(gameDirectory);
    }

    /// Returns the represented game directory.
    public GameDirectory getGameDirectory() {
        return gameDirectory;
    }

}
