// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.modpack.curse;

import com.google.gson.annotations.SerializedName;
import com.ddjyx.neohmcl.modpack.ModpackFile;
import com.ddjyx.neohmcl.modpack.ModpackManifest;
import com.ddjyx.neohmcl.modpack.ModpackProvider;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// @author huangyuhui
@JsonSerializable
public record CurseManifest(@SerializedName("manifestType") String manifestType,
                            @SerializedName("manifestVersion") int manifestVersion,
                            @SerializedName("name") String name,
                            @SerializedName("version") String version,
                            @SerializedName("author") String author,
                            @SerializedName("overrides") String overrides,
                            @SerializedName("minecraft") CurseManifestMinecraft minecraft,
                            @SerializedName("files") @Unmodifiable List<CurseManifestFile> files)
        implements ModpackManifest, ModpackManifest.SupportOptional {

    public CurseManifest setFiles(List<CurseManifestFile> files) {
        return new CurseManifest(manifestType, manifestVersion, name, version, author, overrides, minecraft, files);
    }

    @Override
    public List<? extends ModpackFile> getFiles() {
        return files;
    }

    @Override
    public ModpackProvider getProvider() {
        return CurseModpackProvider.INSTANCE;
    }

    public static final String MINECRAFT_MODPACK = "minecraftModpack";
}
