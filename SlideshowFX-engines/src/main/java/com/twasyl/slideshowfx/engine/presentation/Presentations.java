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

package com.twasyl.slideshowfx.engine.presentation;

import java.util.HashSet;
import java.util.Set;

/**
 * Classes managing all presentations that are currently opened.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class Presentations {

    private static final Set<PresentationEngine> openedPresentations = new HashSet<>();
    private static PresentationEngine currentDisplayedPresentation = null;

    /**
     * Register a given {@link PresentationEngine presentation} as opened.
     * @param presentation The presentation to register.
     */
    public static void register(final PresentationEngine presentation) {
        openedPresentations.add(presentation);
    }

    /**
     * Unregister a given {@link PresentationEngine presentation}.
     * @param presentationEngine The presentation to unregister.
     */
    public static void unregister(final PresentationEngine presentationEngine) {

    }

    /**
     * Get the presentation considered as displayed.
     * @return The presentation considered as displayed.
     */
    public static PresentationEngine getCurrentDisplayedPresentation() {
        return currentDisplayedPresentation;
    }

    /**
     * Defines the presentation that is considered displayed.
     * @param presentation The presentation to set.
     */
    public static void setCurrentDisplayedPresentation(final PresentationEngine presentation) {
        currentDisplayedPresentation = presentation;
    }
}
