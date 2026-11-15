// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.construct;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import com.ddjyx.neohmcl.ui.Controllers;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.SVG;
import com.ddjyx.neohmcl.util.StringUtils;
import com.ddjyx.neohmcl.util.io.FileUtils;

import java.nio.file.Path;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public class FileSelector extends HBox {
    private final StringProperty value = new SimpleStringProperty();
    private String chooserTitle = "";
    private SelectionMode selectionMode = SelectionMode.FILE;
    private final ObservableList<FileChooser.ExtensionFilter> extensionFilters = FXCollections.observableArrayList();

    JFXButton selectButton = FXUtils.newToggleButton4(SVG.FOLDER_OPEN, 15);

    public enum SelectionMode {
        FILE,
        DIRECTORY,
        FILE_OR_DIRECTORY
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public String getChooserTitle() {
        return chooserTitle;
    }

    public FileSelector setChooserTitle(String chooserTitle) {
        this.chooserTitle = chooserTitle;
        return this;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public FileSelector setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    public FileSelector() {
        JFXTextField customField = new JFXTextField();
        customField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customField, Priority.ALWAYS);
        FXUtils.bindString(customField, valueProperty());

        selectButton.setOnAction(e -> {
            switch (selectionMode) {
                case FILE -> openFileChooser(customField);
                case DIRECTORY -> openDirectoryChooser(customField);
                case FILE_OR_DIRECTORY -> {
                    PopupMenu selectPopupMenu = new PopupMenu();
                    JFXPopup selectModePopup = new JFXPopup(selectPopupMenu);

                    selectPopupMenu.getContent().addAll(
                            new IconedMenuItem(SVG.FILE_OPEN, i18n("selector.choose_file"), () -> openFileChooser(customField), selectModePopup),
                            new IconedMenuItem(SVG.FOLDER_OPEN, i18n("selector.choose_directory"), () -> openDirectoryChooser(customField), selectModePopup)
                    );

                    selectModePopup.show(selectButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, -selectButton.getWidth(), 0);
                }
            }
        });

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(3);
        getChildren().addAll(customField, selectButton);
    }

    private void openFileChooser(JFXTextField customField) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(getExtensionFilters());
        chooser.setTitle(StringUtils.isBlank(chooserTitle) ? i18n("selector.choose_file") : chooserTitle);
        Path file = Controllers.showOpenDialog(chooser);
        if (file != null) {
            String path = FileUtils.getAbsolutePath(file);
            customField.setText(path);
            value.setValue(path);
        }
    }

    private void openDirectoryChooser(JFXTextField customField) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(StringUtils.isBlank(chooserTitle) ? i18n("selector.choose_directory") : chooserTitle);
        Path dir = Controllers.showDialog(chooser);
        if (dir != null) {
            String path = FileUtils.getAbsolutePath(dir);
            customField.setText(path);
            value.setValue(path);
        }
    }
}
