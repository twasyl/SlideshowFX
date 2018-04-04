package com.twasyl.slideshowfx.beans.properties;

import com.twasyl.slideshowfx.icons.Icon;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

/**
 * This bindings converts the status of a given {@link Task} to the name of an icon used by SlideshowFX-icons. The name
 * returned belongs to the {@link com.twasyl.slideshowfx.icons.FontAwesome} enum.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class TaskStatusIconBinding extends ObjectBinding<Icon> {

    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();

    public TaskStatusIconBinding(final Task task) {
        if(task == null) throw new NullPointerException("The task can not be null");

        this.task.set(task);

        super.bind(this.task.get().stateProperty());
    }

    @Override
    protected Icon computeValue() {

        switch(this.task.get().getState()) {
            case SCHEDULED:
            case READY:
            case RUNNING:
                return Icon.SPINNER;
            case CANCELLED:
            case FAILED:
                return Icon.EXCLAMATION_CIRCLE;
            case SUCCEEDED:
                return Icon.CHECK_CIRCLE;
        }

        return null;
    }

    @Override
    public void dispose() {
        super.unbind(this.task.get().stateProperty());
    }
}
