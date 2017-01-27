package com.twasyl.slideshowfx.ui.controls.validators;

import org.junit.Test;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isInteger;
import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Class testing the {@link Validators} class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.3
 */
public class ValidatorsTest {

    @Test
    public void isNotEmptyWithNull() {
        assertFalse(isNotEmpty().isValid(null));
    }

    @Test
    public void isNotEmptyWithEmptyString() {
        assertFalse(isNotEmpty().isValid(""));
    }

    @Test
    public void isNotEmptyWithOnlySpaces() {
        assertFalse(isNotEmpty().isValid("  "));
    }

    @Test
    public void isNotEmptyWithNonEmptyString() {
        assertTrue(isNotEmpty().isValid("Test"));
    }

    @Test
    public void isIntegerWithNull() {
        assertFalse(isInteger().isValid(null));
    }

    @Test
    public void isIntegerWithEmptyString() {
        assertFalse(isInteger().isValid(""));
    }

    @Test
    public void isIntegerWithText() {
        assertFalse(isInteger().isValid("Test"));
    }

    @Test
    public void isIntegerWithOnlySpaces() {
        assertFalse(isInteger().isValid("  "));
    }

    @Test
    public void isIntegerWithNumberAndSpace() {
        assertFalse(isInteger().isValid("12 "));
    }

    @Test
    public void isIntegerWithDouble() {
        assertFalse(isInteger().isValid("10.2"));
    }

    @Test
    public void isIntegerWithInteger() {
        assertTrue(isInteger().isValid("10"));
    }
}
