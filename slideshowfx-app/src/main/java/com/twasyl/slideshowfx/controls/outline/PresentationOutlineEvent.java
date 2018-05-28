package com.twasyl.slideshowfx.controls.outline;

/*
 * Implementation of an {@link Event} that aims to be used within a {@link PresentationOutline}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import javafx.event.Event;
import javafx.event.EventType;

public class PresentationOutlineEvent extends Event {
    public static EventType<PresentationOutlineEvent> SLIDE_MOVED = new EventType<>(Event.ANY, "SLIDE_MOVED");
    public static EventType<PresentationOutlineEvent> SLIDE_DELETED = new EventType<>(Event.ANY, "SLIDE_DELETED");
    public static EventType<PresentationOutlineEvent> SLIDE_DELETION_REQUESTED = new EventType<>(Event.ANY, "SLIDE_DELETION_REQUESTED");

    private String sourceSlideId;
    private String targetSlideId;

    public PresentationOutlineEvent(EventType<PresentationOutlineEvent> eventType, String sourceSlideId, String targetSlideId) {
        super(eventType);
        this.sourceSlideId = sourceSlideId;
        this.targetSlideId = targetSlideId;
    }

    /**
     * Get the ID of the slide that is the source of this event.
     *
     * @return The ID of the slide that is the source of the event.
     */
    public String getSourceSlideId() {
        return sourceSlideId;
    }

    /**
     * Get the ID of the slide that is the target of this event. Typically, in the case of a {@link #SLIDE_MOVED} event
     * type, it corresponds to the ID of the slide the source slide ID has been moved before.
     *
     * @return The ID of the slide that is the target of the event.
     */
    public String getTargetSlideId() {
        return targetSlideId;
    }
}
