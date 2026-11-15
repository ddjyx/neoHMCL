// SPDX-FileCopyrightText: Copyright (C) 2024 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.ui.construct.SpinnerPane;
import com.ddjyx.neohmcl.ui.decorator.DecoratorPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

/**
 * @author Glavo
 */
public final class WebPage extends SpinnerPane implements DecoratorPage {

    private final ObjectProperty<DecoratorPage.State> stateProperty;

    public WebPage(String title, String content) {
        this.stateProperty = new SimpleObjectProperty<>(DecoratorPage.State.fromTitle(title));
        this.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        Task.supplyAsync(() -> {
            Document document = Jsoup.parseBodyFragment(content);
            HTMLRenderer renderer = new HTMLRenderer(Controllers::openUriOrCopy);
            renderer.appendNode(document);
            renderer.mergeLineBreaks();
            return renderer.render();
        }).whenComplete(Schedulers.javafx(), ((result, exception) -> {
            if (exception == null) {
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setContent(result);
                setContent(scrollPane);
                setFailedReason(null);
            } else {
                LOG.warning("Failed to load content", exception);
                setFailedReason(i18n("web.failed"));
            }
        })).start();
    }

    @Override
    public ReadOnlyObjectProperty<DecoratorPage.State> stateProperty() {
        return stateProperty;
    }
}
