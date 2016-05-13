package com.twasyl.slideshowfx.utils.concurrent;

import javafx.concurrent.Task;

/**
 * This class allows to perform some actions for a given {@link javafx.concurrent.Task} by adding predicates on it.
 * You can also read documentation for {@link com.twasyl.slideshowfx.utils.concurrent.ForPredicate},
 * {@link com.twasyl.slideshowfx.utils.concurrent.WhenPredicate}, {@link com.twasyl.slideshowfx.utils.concurrent.DoPredicate}
 * classes which are used as predicates.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class TaskAction {

    /**
     * Creates a {@link com.twasyl.slideshowfx.utils.concurrent.ForPredicate} attached to the given {@code task}.
     *
     * @param task The task to perform actions on.
     * @return A well created {@link com.twasyl.slideshowfx.utils.concurrent.ForPredicate}.
     * @throws java.lang.NullPointerException if the given {@code task} is null.
     */
    public static ForPredicate forTask(final Task task) {
        if(task == null) throw new NullPointerException("The task can not be null");

        final ForPredicate forPredicate = new ForPredicate(task);

        return forPredicate;
    }
}
