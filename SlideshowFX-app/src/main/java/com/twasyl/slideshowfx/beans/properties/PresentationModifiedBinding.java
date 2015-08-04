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

package com.twasyl.slideshowfx.beans.properties;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringBinding;

/**
 * This provides a binding to indicate if a presentation s considered as modified since the latest time it was saved.
 * The {@link #computeValue()} method returns an empty String if the presentation is not considered as modified, or
 * {@code *} if it is.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class PresentationModifiedBinding extends StringBinding {

    private BooleanExpression presentationModified;

    /**
     * Construct a StringBinding returning a String according the fact a presentation has been modified or not.
     * @param presentationModified The property to bind to and create the binding for.
     * @throws java.lang.NullPointerException If the given {@code file} is null.
     */
    public PresentationModifiedBinding(BooleanExpression presentationModified) {
        if(presentationModified== null) throw new NullPointerException("The property can not be null");

        this.presentationModified = presentationModified;
        super.bind(this.presentationModified);
    }

    /**
     * Convert the state of the presentation (saved or not since the latest save) to a String.
     * @return An empty String if no modification has been done since the latest save of the presentation, {@code *} otherwise.
     */
    @Override
    protected String computeValue() {
        return this.presentationModified.get() == false ? "" : "*";
    }

    @Override
    public void dispose() {
        super.unbind(this.presentationModified);
    }
}
