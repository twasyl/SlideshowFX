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
import com.twasyl.slideshowfx.utils.DialogHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger LOGGER = Logger.getLogger(Notification.class.getName());
    private final ReadOnlyObjectProperty<Task> task = new SimpleObjectProperty<>();

    public Notification(final Task task) {
        ((SimpleObjectProperty) this.task).set(task);

        final Text taskTitle = this.getTaskTitle();
        final Text statusChangeTime = this.getStatusChangeTimeText();

        final TextFlow statusFlow = new TextFlow(taskTitle, new Text("\n"), statusChangeTime);
        statusFlow.setMaxWidth(250);

        final FontAwesomeIconView statusIcon = this.getStatusIcon();

        final Button deleteButton = this.getDeleteButton();

        final HBox content = new HBox(5);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(statusIcon, statusFlow, deleteButton);

        this.setGraphic(content);

        this.setOnAction(event -> {
            if (this.task.get().getState() == Worker.State.FAILED) {

                final StringBuilder builder = new StringBuilder(this.task.get().getException().getMessage())
                        .append("\n");

                try (final StringWriter stringWriter = new StringWriter();
                    final PrintWriter writer = new PrintWriter(stringWriter)) {

                    this.task.get().getException().printStackTrace(writer);
                    writer.flush();

                    builder.append(stringWriter.toString());
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Can not parse error stacktrace", ex);
                }

                final TextArea errorMessage = new TextArea(builder.toString());
                errorMessage.setPrefColumnCount(50);
                errorMessage.setPrefRowCount(20);
                errorMessage.setWrapText(true);
                errorMessage.setEditable(false);
                errorMessage.setBorder(Border.EMPTY);
                DialogHelper.showError(this.task.get().getTitle(), errorMessage);
            }
        });
    }

    /**
     * Initialize the {@link javafx.scene.Node} that displays the {@link Task#titleProperty() title} of the task.
     * @return The {@link javafx.scene.Node} that displays the title of the task.
     */
    private Text getTaskTitle() {
        final Text taskTitle = new Text();
        taskTitle.getStyleClass().addAll("text", "notification", "title");
        taskTitle.textProperty().bind(this.task.get().titleProperty());

        return taskTitle;
    }

    /**
     * Initialize the {@link javafx.scene.Node} that displays the time when the status of the task has changed.
     * @return The {@link javafx.scene.Node} that displays the change time of the status of the task.
     */
    private Text getStatusChangeTimeText() {
        final Text statusChangeTime = new Text();
        statusChangeTime.getStyleClass().addAll("text", "notification", "time");
        statusChangeTime.textProperty().bind(new TaskStatusTimeBinding(this.task.get()));

        return statusChangeTime;
    }

    /**
     * Initialize the {@link javafx.scene.Node} that will contain the icon indicating the status (RUNNING, SUCCEEDED, FAILED, ...) of the
     * notification.
     * @return The {@link javafx.scene.Node} indicating the status of the notification.
     */
    private FontAwesomeIconView getStatusIcon() {
        final FontAwesomeIconView statusIcon = new FontAwesomeIconView();

        final RotateTransition rotation = new RotateTransition(Duration.seconds(1), statusIcon);
        rotation.setByAngle(360);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setInterpolator(Interpolator.LINEAR);

        statusIcon.glyphNameProperty().addListener((glyphValue, oldGlyph, newGlyph) -> {
            if(FontAwesomeIcon.SPINNER.name().equals(newGlyph)) rotation.playFromStart();
            else {
                rotation.stop();
                statusIcon.setRotate(0);
            }
        });
        statusIcon.glyphNameProperty().bind(new TaskStatusGlyphNameBinding(this.task.get()));
        statusIcon.glyphStyleProperty().bind(new TaskStatusGlyphStyleBinding(this.task.get()));
        statusIcon.setGlyphSize(20);


        return statusIcon;
    }

    /**
     * Initialize the {@link Button} that will allow to remove the notification from the {@link NotificationCenter}.
     * @return The button for deleting the notification from the {@link NotificationCenter}.
     */
    private Button getDeleteButton() {
        final FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        deleteIcon.setGlyphSize(10);

        final Button deleteButton = new Button();
        deleteButton.getStyleClass().add("notification");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(event -> Notification.this.getParentPopup().getItems().remove(Notification.this));

        return deleteButton;
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
