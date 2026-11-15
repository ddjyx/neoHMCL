// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.instances;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.image.Image;
import com.ddjyx.neohmcl.game.HMCLGameInstance;
import com.ddjyx.neohmcl.setting.GameDirectoryManager;
import com.ddjyx.neohmcl.setting.GameInstanceIconType;
import com.ddjyx.neohmcl.ui.FXUtils;
import com.ddjyx.neohmcl.ui.WeakListenerHolder;
import com.ddjyx.neohmcl.ui.construct.AdvancedListItem;
import com.ddjyx.neohmcl.ui.construct.ImageContainer;
import org.jetbrains.annotations.Nullable;

import static com.ddjyx.neohmcl.util.i18n.I18n.i18n;

public class GameAdvancedListItem extends AdvancedListItem {
    private final ImageContainer imageContainer;
    private final WeakListenerHolder holder = new WeakListenerHolder();

    /// Strongly held so [WeakChangeListener] keeps delivering icon updates.
    private final ChangeListener<Image> iconListener;

    private @Nullable WeakChangeListener<Image> weakIconListener;
    private @Nullable ReadOnlyObjectProperty<Image> observedIcon;

    /// Creates an item that follows the selected game instance and its icon.
    public GameAdvancedListItem() {
        this.imageContainer = new ImageContainer(LEFT_GRAPHIC_SIZE);
        this.iconListener = (observable, oldImage, newImage) -> imageContainer.setImage(newImage);
        imageContainer.setMouseTransparent(true);
        setLeftGraphic(imageContainer);

        holder.add(FXUtils.onWeakChangeAndOperate(GameDirectoryManager.selectedInstanceProperty(), this::loadInstance));
    }

    private void loadInstance(@Nullable HMCLGameInstance instance) {
        unbindIcon();
        if (instance != null) {
            setTitle(i18n("instance.manage.manage"));
            setSubtitle(instance.getId().toString());
            observedIcon = instance.iconImageProperty();
            weakIconListener = new WeakChangeListener<>(iconListener);
            observedIcon.addListener(weakIconListener);
            imageContainer.setImage(instance.getIconImage());
            return;
        }

        setTitle(i18n("instance.empty"));
        setSubtitle(i18n("instance.empty.add"));
        imageContainer.setImage(GameInstanceIconType.DEFAULT.getIcon());
    }

    private void unbindIcon() {
        if (observedIcon != null && weakIconListener != null) {
            observedIcon.removeListener(weakIconListener);
        }
        observedIcon = null;
        weakIconListener = null;
    }
}
