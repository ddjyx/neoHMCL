// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.setting;

import com.google.gson.JsonParseException;
import com.ddjyx.neohmcl.auth.authlibinjector.AuthlibInjectorServer;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.gson.JsonUtils;
import com.ddjyx.neohmcl.util.gson.TolerableValidationException;
import com.ddjyx.neohmcl.util.gson.Validation;
import com.ddjyx.neohmcl.util.io.JarUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.ddjyx.neohmcl.setting.SettingsManager.settings;
import static com.ddjyx.neohmcl.setting.SettingsManager.getAuthlibInjectorServers;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

@JsonSerializable
public final class AuthlibInjectorServers implements Validation {

    public static final String CONFIG_FILENAME = "authlib-injectors.json";

    private static final Set<AuthlibInjectorServer> servers = new CopyOnWriteArraySet<>();

    public static Set<AuthlibInjectorServer> getServers() {
        return servers;
    }

    private final List<String> urls;

    private AuthlibInjectorServers(List<String> urls) {
        this.urls = urls;
    }

    @Override
    public void validate() throws JsonParseException, TolerableValidationException {
        if (this.urls == null) {
            throw new JsonParseException("authlib-injectors.json -> urls cannot be null.");
        }
    }

    public static void init() {
        Path configLocation;
        Path jarPath = JarUtils.thisJarPath();
        if (jarPath != null && Files.isRegularFile(jarPath) && Files.isWritable(jarPath)) {
            configLocation = jarPath.getParent().resolve(CONFIG_FILENAME);
        } else {
            configLocation = Paths.get(CONFIG_FILENAME);
        }

        if (SettingsManager.isNewlyCreated() && Files.exists(configLocation)) {
            AuthlibInjectorServers configInstance;
            try {
                configInstance = JsonUtils.fromJsonFile(configLocation, AuthlibInjectorServers.class);
            } catch (IOException | JsonParseException e) {
                LOG.warning("Malformed authlib-injectors.json", e);
                return;
            }

            if (!configInstance.urls.isEmpty()) {
                settings().preferredLoginTypeProperty().set(Accounts.getLoginType(Accounts.FACTORY_AUTHLIB_INJECTOR));
                for (String url : configInstance.urls) {
                    Task.supplyAsync(Schedulers.io(), () -> AuthlibInjectorServer.locateServer(url))
                            .thenAcceptAsync(Schedulers.javafx(), server -> {
                                getAuthlibInjectorServers().add(server);
                                servers.add(server);
                            })
                            .start();
                }
            }
        }
    }
}
