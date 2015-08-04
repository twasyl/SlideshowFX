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
