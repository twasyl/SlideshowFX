/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.notification;

import com.twasyl.slideshowfx.beans.properties.TaskStatusGlyphNameBinding;
import com.twasyl.slideshowfx.beans.properties.TaskStatusGlyphStyleBinding;
import com.twasyl.slideshowfx.beans.properties.TaskStatusTimeBinding;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Represents a notification to be present in the {@link NotificationCenter}. It displays the title of the {@link Task}
 * that is associated to the notification, as well as an icon representing the status of the task and a button for deleting
 * the notification in the notification center.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Notification extends MenuItem {

    private final ReadOnlyObjectProperty<Task> task = new SimpleObjectProperty<>();

    public Notification(final Task task) {

        final Text taskTitle = new Text();
        taskTitle.getStyleClass().addAll("text", "notification", "title");
        taskTitle.textProperty().bind(task.titleProperty());

        final Text statusChangeTime = new Text();
        statusChangeTime.getStyleClass().addAll("text", "notification", "time");
        statusChangeTime.textProperty().bind(new TaskStatusTimeBinding(task));

        final TextFlow statusFlow = new TextFlow(taskTitle, new Text("\n"), statusChangeTime);
        statusFlow.setMaxWidth(250);

        final FontAwesomeIconView statusButton = new FontAwesomeIconView();
        statusButton.glyphNameProperty().bind(new TaskStatusGlyphNameBinding(task));
        statusButton.glyphStyleProperty().bind(new TaskStatusGlyphStyleBinding(task));
        statusButton.setGlyphSize(20);

        final FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        deleteIcon.setGlyphSize(10);

        final Button deleteButton = new Button();
        deleteButton.getStyleClass().add("notification");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(event -> Notification.this.getParentPopup().getItems().remove(Notification.this));

        final HBox content = new HBox(5);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(statusButton, statusFlow, deleteButton);

        ((SimpleObjectProperty) this.task).set(task);

        this.setGraphic(content);
    }

    /**
     * Get the task associated to this notification.
     * @return The property containing the task associated to this notification.
     */
    public ReadOnlyObjectProperty<Task> taskProperty() { return task; }

    /**
     * Get the task associated to this notification.
     * @return The task associated to this notification.
     */
    public Task getTask() { return task.get(); }
}
