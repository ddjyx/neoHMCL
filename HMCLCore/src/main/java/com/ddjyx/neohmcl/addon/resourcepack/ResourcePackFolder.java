// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.addon.resourcepack;

import javafx.scene.image.Image;
import com.ddjyx.neohmcl.addon.meta.PackMcMeta;
import com.ddjyx.neohmcl.util.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

final class ResourcePackFolder extends ResourcePackFile {
    private final PackMcMeta.PackInfo info;

    public ResourcePackFolder(ResourcePackManager manager, Path path) {
        super(manager, path);

        PackMcMeta meta = null;
        try {
            meta = PackMcMeta.fromNonNullJsonFile(path.resolve("pack.mcmeta"));
        } catch (Exception e) {
            LOG.warning("Failed to parse resource pack meta", e);
        }
        this.info = meta != null ? meta.pack() : null;
    }

    @Override
    public PackMcMeta.PackInfo getPackInfo() {
        return info;
    }

    @Override
    public @Nullable Image loadIcon() {
        byte[] iconData = null;
        try {
            iconData = Files.readAllBytes(getFile().resolve("pack.png"));
        } catch (Exception e) {
            LOG.warning("Failed to load resource pack icon", e);
        }

        if (iconData != null) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(iconData)) {
                return new Image(inputStream, 64, 64, true, true);
            } catch (Exception e) {
                LOG.warning("Failed to load resource pack icon", e);
            }
        }
        return null;
    }

    @Override
    public void delete() throws IOException {
        FileUtils.deleteDirectory(file);
    }
}
