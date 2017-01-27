package com.twasyl.slideshowfx.controls.slideshow;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;

/**
 * <p>This class serves as a context to create a:</p>
 * <ul>
 *     <li>{@link SlideshowStage}</li>
 *     <li>{@link SlideshowPane}</li>
 *     <li>{@link InformationPane}</li>
 * </ul>
 * <p>It specifies  what is the presentation to use, at which slide it must start, and so on.</p>
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 * @see SlideshowStage
 * @see SlideshowPane
 * @see InformationPane
 */
public class Context {
    private String startAtSlideId;
    private PresentationEngine presentation;

    /**
     * Indicates at which slide the slideshow must start. If nothing is specified, {@code null} is returned and the
     * slideshow will start from the begining.
     * @return The slide ID where the slideshow must start or {@code null} if it hasn't been defined.
     */
    public String getStartAtSlideId() { return startAtSlideId; }

    /**
     * Defines at which slide the slideshow must start. If {@code null} is passed as parameter, the slideshow will
     * start from the beginning.
     * @param startAtSlideId
     */
    public void setStartAtSlideId(String startAtSlideId) { this.startAtSlideId = startAtSlideId; }

    /**
     * Get the presentation associated to this context.
     * @return The presentation associated to this context.
     */
    public PresentationEngine getPresentation() { return presentation; }

    /**
     * Defines the presentation that will be associated to this context.
     * @param presentation The presentation associated to this context.
     */
    public void setPresentation(PresentationEngine presentation) {
        this.presentation = presentation;
    }
}
