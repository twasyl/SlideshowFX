package com.twasyl.slideshowfx.events;

/**
 * Represent an event indicating a slide has changed, typically within a {@link com.twasyl.slideshowfx.controls.PresentationBrowser}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class SlideChangedEvent {
    private String currentSlide;

    public SlideChangedEvent() {
    }

    public SlideChangedEvent(String currentSlide) {
        this.currentSlide = currentSlide;
    }

    public String getCurrentSlide() {
        return currentSlide;
    }

    public void setCurrentSlide(String currentSlide) {
        this.currentSlide = currentSlide;
    }
}
