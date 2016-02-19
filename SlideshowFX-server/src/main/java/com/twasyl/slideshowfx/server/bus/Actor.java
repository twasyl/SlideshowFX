package com.twasyl.slideshowfx.server.bus;

/**
 * Defines an actor that can be register in the {@link EventBus}.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public interface Actor {

    /**
     * Tests if the given message is supported by this actor. A message can be considered as supported if it is not
     * {@code null} or if it is an instance of a given class. The criteria of supported message is determined by the
     * implementing class.
     *
     * @param message The message to test if it is supported or not.
     * @return {@code true} if te message is considered as supported, {@code false} otherwise.
     */
    boolean supportsMessage(final Object message);

    /**
     * Performs an action when the message is sent. This method is only called by the {@link EventBus} if the message is
     * considered as supported by this actor, as defined by the {@link #supportsMessage(Object)} method. This avoids
     * implementations of this method to take care of the message support.
     *
     * @param message The message sent.
     */
    void onMessage(final Object message);
}
