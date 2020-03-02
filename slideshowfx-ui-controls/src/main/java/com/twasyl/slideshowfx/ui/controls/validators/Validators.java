package com.twasyl.slideshowfx.ui.controls.validators;

/**
 * Class providing implementation of {@link IValidator}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.3
 */
public final class Validators {

    /**
     * Creates a validator checking a given string is not empty. If the string is {@code null}, the string is
     * considered invalid.
     *
     * @return A validator checking non empty strings.
     */
    public static IValidator<String> isNotEmpty() {
        return value -> value != null && !value.isBlank();
    }

    /**
     * Creates a validator checking a given string is an integer.
     *
     * @return A validator checking integer strings.
     */
    public static IValidator<String> isInteger() {
        return value -> {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        };
    }

    public static IValidator<String> isIntegerOrBlank() {
        return value -> {
            if (value == null || value.isBlank()) {
                return true;
            } else {
                return isInteger().isValid(value);
            }
        };
    }

    /**
     * Creates a validator checking a given string is a double.
     *
     * @return A validator checking double strings.
     */
    public static IValidator<String> isDouble() {
        return value -> {
            try {
                Double.parseDouble(value);
                return true;
            } catch(NumberFormatException ex) {
                return false;
            }
        };
    }
}
