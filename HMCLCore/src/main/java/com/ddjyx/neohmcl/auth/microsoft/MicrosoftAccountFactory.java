// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.auth.microsoft;

import com.google.gson.JsonObject;
import com.ddjyx.neohmcl.auth.Account;
import com.ddjyx.neohmcl.auth.AccountFactory;
import com.ddjyx.neohmcl.auth.AccountID;
import com.ddjyx.neohmcl.auth.AuthenticationException;
import com.ddjyx.neohmcl.auth.CharacterSelector;
import com.ddjyx.neohmcl.auth.OAuth;
import com.ddjyx.neohmcl.auth.yggdrasil.CompleteGameProfile;
import com.ddjyx.neohmcl.auth.yggdrasil.GameProfile;
import com.ddjyx.neohmcl.util.gson.JsonUtils;

import java.util.Map;
import java.util.Objects;

public class MicrosoftAccountFactory extends AccountFactory<MicrosoftAccount> {

    private final MicrosoftService service;

    public MicrosoftAccountFactory(MicrosoftService service) {
        this.service = service;
    }

    @Override
    public AccountLoginType getLoginType() {
        return AccountLoginType.NONE;
    }

    @Override
    public MicrosoftAccount create(CharacterSelector selector, String username, String password, ProgressCallback progressCallback, Object additionalData) throws AuthenticationException {
        return new MicrosoftAccount(service, (OAuth.GrantFlow) additionalData);
    }

    @Override
    public MicrosoftAccount fromStorage(JsonObject metadata, JsonObject privateData) {
        Objects.requireNonNull(metadata);
        Objects.requireNonNull(privateData);
        AccountID accountID = Account.readAccountID(metadata);
        MicrosoftSession session = MicrosoftSession.fromStorage(metadata, privateData);

        if (privateData.get("profileProperties") instanceof JsonObject profilePropertiesObject) {
            Map<String, String> properties = JsonUtils.GSON.fromJson(
                    profilePropertiesObject,
                    JsonUtils.mapTypeOf(String.class, String.class));
            MicrosoftSession.GameProfile profile = session.profile();
            var profileRepository = service.getProfileRepository();
            profileRepository.put(profile.id(), new CompleteGameProfile(new GameProfile(profile.id(), profile.name()), properties));
            profileRepository.invalidate(profile.id());
        }

        return new MicrosoftAccount(accountID, service, session);
    }
}
