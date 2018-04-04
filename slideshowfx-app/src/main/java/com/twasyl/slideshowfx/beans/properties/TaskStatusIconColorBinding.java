package com.twasyl.slideshowfx.beans.properties;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

/**
 * This binding converts the status of a given {@link Task} to a string defining the CSS color a
 * {@link com.twasyl.slideshowfx.icons.FontAwesome} can use.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class TaskStatusIconColorBinding extends StringBinding {

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();

    public TaskStatusIconColorBinding(final Task task) {
        if(task == null) throw new NullPointerException("The task can not be null");

        this.task.set(task);

        super.bind(this.task.get().stateProperty());
    }

    @Override
    protected String computeValue() {

        final StringBuilder style = new StringBuilder();

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
