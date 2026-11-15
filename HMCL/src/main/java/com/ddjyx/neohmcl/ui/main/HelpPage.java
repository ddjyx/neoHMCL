// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.main;

import com.google.gson.annotations.SerializedName;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.ddjyx.neohmcl.Metadata;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.ui.ScrollUtils;
import com.ddjyx.neohmcl.ui.construct.ComponentList;
import com.ddjyx.neohmcl.ui.construct.LineButton;
import com.ddjyx.neohmcl.ui.construct.SpinnerPane;
import com.ddjyx.neohmcl.util.gson.JsonSerializable;
import com.ddjyx.neohmcl.util.io.HttpRequest;

import java.util.ArrayList;
import java.util.List;

import static com.ddjyx.neohmcl.util.gson.JsonUtils.listTypeOf;
import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;
import static com.ddjyx.neohmcl.util.logging.Logger.LOG;

public class HelpPage extends SpinnerPane {

    private final VBox content;

    public HelpPage() {
        content = new VBox();
        content.getStyleClass().add("spinner-pane-content");
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        ScrollUtils.installSmoothScrolling(scrollPane);
        setContent(scrollPane);

        var docPane = LineButton.createExternalLinkButton(Metadata.DOCS_URL);
        docPane.setLargeTitle(true);
        docPane.setTitle(i18n("help.doc"));
        docPane.setSubtitle(i18n("help.detail"));

        ComponentList doc = new ComponentList();
        doc.getContent().setAll(docPane);
        content.getChildren().add(doc);

        loadHelp();
    }

    private void loadHelp() {
        showSpinner();
        Task.supplyAsync(() -> HttpRequest.GET(Metadata.DOCS_URL + "/index.json").getJson(listTypeOf(HelpCategory.class)))
            .whenComplete(Schedulers.javafx(), (helpCategories, exception) -> {
                if (exception != null) {
                    LOG.warning("Failed to load help page: ", exception);
                    showLoadFailure();
                    return;
                }

                try {
                    List<Node> nodes = new ArrayList<>();

                    for (HelpCategory category : helpCategories) {
                        ComponentList categoryPane = new ComponentList();

                        for (Help help : category.items()) {
                            var item = LineButton.createExternalLinkButton(help.url());
                            item.setLargeTitle(true);
                            item.setTitle(help.title());
                            item.setSubtitle(help.subtitle());

                            categoryPane.getContent().add(item);
                        }

                        nodes.add(ComponentList.createComponentListTitle(category.title()));
                        nodes.add(categoryPane);
                    }

                    content.getChildren().addAll(nodes);

                    hideSpinner();
                } catch (Exception e) {
                    LOG.warning("Failed to display help page: ", e);
                    showLoadFailure();
                }
            }).start();
    }

    private void showLoadFailure() {
        setFailedReason(i18n("download.failed.refresh"));
        setOnFailedAction(e -> loadHelp());
        setLoading(false);
    }

    @JsonSerializable
    private record HelpCategory(
            @SerializedName("title") String title,
            @SerializedName("items") List<Help> items) {
    }

    @JsonSerializable
    private record Help(
            @SerializedName("title") String title,
            @SerializedName("subtitle") String subtitle,
            @SerializedName("url") String url) {
    }
}
