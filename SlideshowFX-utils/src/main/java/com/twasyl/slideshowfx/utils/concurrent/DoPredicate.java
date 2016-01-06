/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.utils.concurrent;

/**
 * A DoPredicate is created by {@link com.twasyl.slideshowfx.utils.concurrent.WhenPredicate} instances.
 * The {@link #perform(Runnable)} method must be called in order an action to be performed by the listener
 * added on the task by the {@link com.twasyl.slideshowfx.utils.concurrent.WhenPredicate}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class DoPredicate {

    /**
     * The WhenPredicate this predicate belongs to.
     */
    protected WhenPredicate whenPredicate;

    /**
     * The action to be performed when the listener added on the task is triggered.
     */
    protected Runnable runnable;

    protected DoPredicate(final WhenPredicate whenPredicate) {
        this.whenPredicate = whenPredicate;
    }

    /**
     * Defines the action that will be performed when the listener added by the {@link com.twasyl.slideshowfx.utils.concurrent.WhenPredicate}
     * on a properties of a task is triggered.
     *
     * @param runnable The action to perform.
     * @return The {@link com.twasyl.slideshowfx.utils.concurrent.ForPredicate} that is parent of the {@link com.twasyl.slideshowfx.utils.concurrent.WhenPredicate}
     * this predicate belongs to.
     * @throws java.lang.NullPointerException if {@code runnable} is null.
     */
    public ForPredicate perform(Runnable runnable) {
        if(runnable == null) throw new NullPointerException("The action to perform can not be null");
        this.runnable = runnable;
        return this.whenPredicate.forPredicate;
    }
}
