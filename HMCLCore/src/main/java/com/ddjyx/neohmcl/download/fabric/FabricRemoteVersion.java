// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.fabric;

import org.glavo.url.WebURL;
import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.download.ComponentRemoteVersion;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;
import java.util.List;

@NotNullByDefault
public final class FabricRemoteVersion extends ComponentRemoteVersion {
    public static final WebURL LOADER_META_URL = WebURL.parse("https://meta.fabricmc.net/v2/versions/loader");
    public static final WebURL GAME_META_URL = WebURL.parse("https://meta.fabricmc.net/v2/versions/game");

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param urls        the installer or universal jar original URL.
     */
    public FabricRemoteVersion(GameVersionNumber gameVersion, String selfVersion, List<String> urls) {
        super(GameComponentType.FABRIC, gameVersion, selfVersion, null, Type.UNCATEGORIZED, urls);
    }

    @Override
    public Task<GameInstancePatch> getInstallTask(DefaultDependencyManager dependencyManager, GameInstanceManifest baseManifest, Path modsDirectory) {
        return new FabricInstallTask(dependencyManager, baseManifest, this);
    }
}
