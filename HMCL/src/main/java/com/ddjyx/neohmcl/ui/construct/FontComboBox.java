// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.construct;

import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableList;
import static javafx.collections.FXCollections.singletonObservableList;

import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.util.javafx.BindingMapping;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;

import javafx.beans.binding.Bindings;
import javafx.scene.text.Font;

public final class FontComboBox extends JFXComboBox<String> {

    private boolean loaded = false;

    public FontComboBox() {
        setMinWidth(260);

        styleProperty().bind(Bindings.concat("-fx-font-family: \"", valueProperty(), "\""));

        setCellFactory(listView -> new JFXListCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText(item);
                    setGraphic(null);
                    setStyle("-fx-font-family: \"" + item + "\"");
                }
            }
        });

        itemsProperty().bind(BindingMapping.of(valueProperty())
                        .map(value -> value == null ? emptyObservableList() : singletonObservableList(value)));

        FXUtils.onClicked(this, () -> {
            if (loaded)
                return;
            itemsProperty().unbind();
            setItems(observableList(Font.getFamilies()));
            loaded = true;
        });
    }
}
