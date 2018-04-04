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
