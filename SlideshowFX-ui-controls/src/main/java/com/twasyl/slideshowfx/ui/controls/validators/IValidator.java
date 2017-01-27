package com.twasyl.slideshowfx.ui.controls.validators;

/**
 * A validator allows to perform operations to determine if a given {@link Object} is valid. The {@link Validators}
 * class provides a set of {@link IValidator}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.3
 */
public interface IValidator<T> {

    /**
     * Check if a given value is considered as valid.
     *
     * @param value The value to check.
     * @return {@code true} if the value is considered valid, {@code false} otherwise.
     */
    boolean isValid(T value);
}
