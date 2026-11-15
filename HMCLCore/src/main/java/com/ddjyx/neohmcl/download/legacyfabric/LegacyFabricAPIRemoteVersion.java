// SPDX-FileCopyrightText: Copyright (C) 2022 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.download.legacyfabric;

import com.ddjyx.neohmcl.download.DefaultDependencyManager;
import com.ddjyx.neohmcl.download.ComponentRemoteVersion;
import com.ddjyx.neohmcl.game.GameComponentType;
import com.ddjyx.neohmcl.game.GameInstanceManifest;
import com.ddjyx.neohmcl.game.GameInstancePatch;
import com.ddjyx.neohmcl.addon.RemoteAddon;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNullByDefault;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@NotNullByDefault
public final class LegacyFabricAPIRemoteVersion extends ComponentRemoteVersion {

    /// @see <a href="https://modrinth.com/mod/legacy-fabric-api">Legacy Fabric API - Modrinth</a>
    public static final String MODRINTH_ID = "legacy-fabric-api";

    private final String fullVersion;
    private final RemoteAddon.Version version;

    /**
     * Constructor.
     *
     * @param gameVersion the Minecraft version that this remote version suits.
     * @param selfVersion the version string of the remote version.
     * @param urls        the installer or universal jar original URL.
     */
    public LegacyFabricAPIRemoteVersion(GameVersionNumber gameVersion, String selfVersion, String fullVersion, Instant datePublished, RemoteAddon.Version version, List<String> urls) {
        super(GameComponentType.LEGACY_FABRIC_API, gameVersion, selfVersion, datePublished, Type.UNCATEGORIZED, urls);

        this.fullVersion = fullVersion;
        this.version = version;
    }

    @Override
    public String getFullVersion() {
        return fullVersion;
    }

    public RemoteAddon.Version getVersion() {
        return version;
    }

    @Override
    public Task<GameInstancePatch> getInstallTask(
            DefaultDependencyManager dependencyManager,
            GameInstanceManifest baseManifest,
            Path modsDirectory) {
        return new LegacyFabricAPIInstallTask(dependencyManager, baseManifest, this, modsDirectory);
    }

    @Override
    public int compareTo(ComponentRemoteVersion o) {
        if (!(o instanceof LegacyFabricAPIRemoteVersion)) return 0;
        return -this.getReleaseDate().compareTo(o.getReleaseDate());
    }
}
