/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.notification;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.HashSet;

/**
 * A center that contains multiple {@link Notification}. The center displays a progress for the task that is considered
 * as currently running, as well as label for indicating the task's action.
 * Clicking on the progress will display all tasks that are currently registered to the center. Each task is wrapped
 * within a {@link Notification} and can be unregistered from the center.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class NotificationCenter extends StackPane {

    /*
     * Properties of the notification center
     */
    private final ReadOnlySetProperty<Task> registeredTasks = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));
    private final ObjectProperty<Task> currentTask = new SimpleObjectProperty<>();

    /*
     * UI elements
     */
    private final ProgressBar currentTaskProgress = new ProgressBar(0);
    private final Label currentTaskMessage = new Label();

    private final SequentialTransition labelTransition = new SequentialTransition();

    public NotificationCenter() {
        this.initAnimations();
        this.initStatusListeners();
        this.initProgressBar();

        final HBox currentNotificationPane = new HBox(5);
        currentNotificationPane.getChildren().addAll(this.currentTaskProgress, this.currentTaskMessage);

        this.getChildren().add(currentNotificationPane);
    }

    /**
     * Initialize all animation used within the notification center.
     */
    private final void initAnimations() {
        final PauseTransition pause = new PauseTransition(Duration.seconds(2));

        final FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), this.currentTaskMessage);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        this.labelTransition.setNode(this.currentTaskMessage);
        this.labelTransition.getChildren().addAll(pause, fadeOut);
    }

    /**
     * Initialize listeners that update the UI when the current task changes or it's status.
     */
    private final void initStatusListeners() {
        // Initialize the UI elements for responding to the current task status
        this.currentTaskMessage.textProperty().addListener((textValue, oldText, newText) -> {
            this.labelTransition.playFromStart();
        });

        // The listener to the task will update the label and the progress bar for the new task
        this.currentTask.addListener((taskValue, oldTask, newtask) -> {
            if (this.currentTaskProgress.progressProperty().isBound()) {
                this.currentTaskProgress.progressProperty().unbind();
            }
            if (this.currentTaskMessage.textProperty().isBound()) {
                this.currentTaskMessage.textProperty().unbind();
            }

            if (newtask != null) {
                this.currentTaskMessage.textProperty().bind(newtask.messageProperty());
                this.currentTaskProgress.progressProperty().bind(newtask.progressProperty());
            }
        });
    }

    /**
     * Initialize the progress bar, like add listener when clicking on it for displaying all tasks.
     */
    private final void initProgressBar() {
        this.currentTaskProgress.setPrefWidth(200);

        this.currentTaskProgress.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                final ContextMenu menu = new ContextMenu();

                this.registeredTasks.stream().forEach(task -> {
                    final Notification notification = new Notification(task);
                    menu.getItems().add(notification);
                });

                menu.getItems().addListener((ListChangeListener) (change) -> {
                    if (change.next() && change.wasRemoved()) {
                        change.getRemoved()
                                . stream()
                                .forEach(notification -> NotificationCenter.this.unregisterTask(((Notification) notification).getTask()));
                    }
                    change.reset();

                    menu.hide();
                    menu.show(this.currentTaskProgress, Side.TOP, 0, 0);
                });
                menu.show(this.currentTaskProgress, Side.TOP, 0, 0);
            }
        });
    }
    /**
     * Get the tasks currently registered to the notification center. tasks are present in the list until the user
     * decides to delete them from the UI.
     * @return The list containing the current tasks of the notification center.
     */
    public ReadOnlySetProperty<Task> registeredTasksProperty() { return registeredTasks; }

    /**
     * Get the tasks currently registered to the notification center. tasks are present in the list until the user
     * decides to delete them from the UI.
     * @return The list containing the current tasks of the notification center.
     */
    public ObservableSet<Task> getRegisteredTasks() { return registeredTasks.get(); }


    public String getText() { return this.currentTaskMessage.getText(); }
    public void setText(String text) { this.currentTaskMessage.setText(text); }

    public double getProgress() { return this.currentTaskProgress.getProgress(); }
    public void setProgress(double progress) { this.currentTaskProgress.setProgress(progress); }

    /**
     * Updates the NotificationCenter with the given progress and text.
     * @param progress The new progress
     * @param text The new text
     */
    public void update(double progress, String text) {
        this.setProgress(progress);
        this.setText(text);

        PlatformHelper.run(() -> this.labelTransition.playFromStart());
    }

    /**
     * Get the current task registered to the notification center. The current task is the one which informations is
     * displayed next to the progress bar.
     * @return The current task of the notification center.
     */
    public ObjectProperty<Task> currentTaskProperty() { return this.currentTask; }

    /**
     * Get the current task registered to the notification center. The current task is the one which informations is
     * displayed next to the progress currentTaskProgress.
     * @return The current task of the notification center.
     */
    public Task getCurrentTask() { return currentTask.get(); }


    /**
     * Defines the given {@code task} as current task of this notification center. The task {@code task} is also
     * registered to the collection of tasks of this notification center.
     * This method adds listeners to the {@link javafx.concurrent.Task#messageProperty()} and
     * {@link javafx.concurrent.Task#progressProperty()} in order to reflect changes to the indicator.
     *
     * @param task The task to set to this indicator
     */
    public void setCurrentTask(final Task task) {
        this.currentTask.set(task);
        this.registerTask(task);
    }

    /**
     * Adds a task to the list of current tasks in the notification center.
     * @param task The task to add.
     * @throws NullPointerException If the provided task is {@code null}.
     */
    public void registerTask(final Task task) throws NullPointerException {
        if(task == null) throw new NullPointerException("The task to add to the notification center can not be null");

        ((SimpleSetProperty) this.registeredTasks).add(task);
    }

    /**
     * Unregister a task from the notification center.
     * @param task The task to remove from the notification center.
     * @throws NullPointerException If the given {@code task} is null.
     */
    public void unregisterTask(final Task task) {
        if(task == null) throw new NullPointerException("The task to unregister from the notification center can not be null");

        ((SimpleSetProperty) this.registeredTasks).remove(task);
    }
}
