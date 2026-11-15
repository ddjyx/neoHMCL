// SPDX-FileCopyrightText: Copyright (C) 2020 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package com.ddjyx.neohmcl.ui.wizard;

import javafx.scene.control.Label;
import com.ddjyx.neohmcl.task.Schedulers;
import com.ddjyx.neohmcl.task.Task;
import com.ddjyx.neohmcl.task.TaskExecutor;
import com.ddjyx.neohmcl.ui.construct.TaskListPane;
import com.ddjyx.neohmcl.util.SettingsMap;
import com.ddjyx.neohmcl.util.TaskManager;

import java.util.Queue;

public abstract class AbstractWizardDisplayer implements WizardDisplayer {
    private final Queue<Object> cancelQueue;

    public AbstractWizardDisplayer(Queue<Object> cancelQueue) {
        this.cancelQueue = cancelQueue;
    }

    /// Displays task progress and submits the wizard's execution to the serial business-task queue.
    @Override
    public void handleTask(SettingsMap settings, Task<?> task) {
        TaskExecutor executor = task.withRunAsync(Schedulers.javafx(), this::navigateToSuccess).executor();
        TaskListPane pane = new TaskListPane();
        pane.setExecutor(executor);
        navigateTo(pane, Navigation.NavigationDirection.FINISH);
        cancelQueue.add(executor);
        TaskManager.getInstance().submit(executor, null);
    }

    @Override
    public void onCancel() {
        while (!cancelQueue.isEmpty()) {
            Object x = cancelQueue.poll();
            if (x instanceof TaskExecutor) ((TaskExecutor) x).cancel();
            else if (x instanceof Thread) ((Thread) x).interrupt();
            else throw new IllegalStateException("Unrecognized cancel queue element: " + x);
        }
    }

    void navigateToSuccess() {
        navigateTo(new Label("Successful"), Navigation.NavigationDirection.FINISH);
    }
}
