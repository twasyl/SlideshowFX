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

package com.twasyl.slideshowfx.beans.properties;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

/**
 * This binding converts the status of a given {@link Task} to a string defining the CSS style a
 * {@link de.jensd.fx.glyphs.GlyphIcon} can use. The style only defines the {@code -fx-fill} property in order to color
 * the icon.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class TaskStatusGlyphStyleBinding extends StringBinding {

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();

    public TaskStatusGlyphStyleBinding(final Task task) {
        if(task == null) throw new NullPointerException("The task can not be null");

        this.task.set(task);

        super.bind(this.task.get().stateProperty());
    }

    @Override
    protected String computeValue() {

        final StringBuilder style = new StringBuilder("-fx-fill: ");

        switch(this.task.get().getState()) {
            case SCHEDULED:
            case READY:
            case RUNNING:
            case CANCELLED:
            case FAILED:
                style.append("app-color-orange");
                break;
            case SUCCEEDED:
                style.append("green");
                break;
        }

        return style.toString();
    }

    @Override
    public void dispose() {
        super.unbind(this.task.get().stateProperty());
    }
}
