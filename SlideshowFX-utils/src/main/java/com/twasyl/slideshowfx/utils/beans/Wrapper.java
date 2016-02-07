package com.twasyl.slideshowfx.utils.beans;

/**
 * A simple wrapper allowing to hold a generic value.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class Wrapper<T> {
    private T value;

    /**
     * Creates an empty wrapper.
     */
    public Wrapper() { }

    /**
     * Creates a wrapper with the given value.
     * @param value The value of the wrapper.
     */
    public Wrapper(T value) {
        this.value = value;
    }

    /**
     * Get the value defined for this wrapper.
     * @return The value of this wrapper.
     */
    public T getValue() { return value; }

    /**
     * Set the value for this wrapper.
     * @param value The value to set for this wrapper.
     */
    public void setValue(T value) { this.value = value; }

    /**
     * Check if this wrapper is considered as empty. A wrapper is considered as empty if its value is equal to
     * {@code null}.
     * @return {@code true} if this wrapper is empty, {@code false} otherwise.
     */
    public boolean isEmpty() { return this.value == null; }
}
