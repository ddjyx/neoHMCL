// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.main;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import com.ddjyx.neohmcl.theme.Themes;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.ScrollUtils;
import com.ddjyx.neohmcl.ui.WeakListenerHolder;
import com.ddjyx.neohmcl.ui.construct.ComponentList;
import com.ddjyx.neohmcl.ui.construct.LineButton;
import com.ddjyx.neohmcl.ui.construct.SpinnerPane;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

import com.ddjyx.neohmcl.Metadata;

public class FeedbackPage extends SpinnerPane {

    private final WeakListenerHolder holder = new WeakListenerHolder();

    public FeedbackPage() {
        VBox content = new VBox();
        content.getStyleClass().add("spinner-pane-content");
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        ScrollUtils.installSmoothScrolling(scrollPane);
        setContent(scrollPane);

        ComponentList groups = new ComponentList();
        {
            var users = LineButton.createExternalLinkButton(Metadata.GROUPS_URL);
            users.setLargeTitle(true);
            users.setLeading(FXUtils.newBuiltinImage("/assets/img/icon.png"));
            users.setTitle(i18n("contact.chat.qq_group"));
            users.setSubtitle(i18n("contact.chat.qq_group.statement"));

            var discord = LineButton.createExternalLinkButton("https://discord.gg/jVvC7HfM6U");
            discord.setLargeTitle(true);
            discord.setLeading(FXUtils.newBuiltinImage("/assets/img/discord.png"));
            discord.setTitle(i18n("contact.chat.discord"));
            discord.setSubtitle(i18n("contact.chat.discord.statement"));

            groups.getContent().setAll(users, discord);
        }

        ComponentList feedback = new ComponentList();
        {
            var github = LineButton.createExternalLinkButton("https://github.com/HMCL-dev/HMCL/issues/new/choose");
            github.setLargeTitle(true);
            github.setTitle(i18n("contact.feedback.github"));
            github.setSubtitle(i18n("contact.feedback.github.statement"));

            holder.add(FXUtils.onWeakChangeAndOperate(Themes.darkModeProperty(), darkMode -> {
                github.setLeading(darkMode
                        ? FXUtils.newBuiltinImage("/assets/img/github-white.png")
                        : FXUtils.newBuiltinImage("/assets/img/github.png"));
            }));

            feedback.getContent().setAll(github);
        }

        content.getChildren().addAll(
                ComponentList.createComponentListTitle(i18n("contact.chat")),
                groups,
                ComponentList.createComponentListTitle(i18n("contact.feedback")),
                feedback
        );
    }
}
