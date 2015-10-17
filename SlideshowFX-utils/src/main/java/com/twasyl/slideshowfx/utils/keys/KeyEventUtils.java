package com.twasyl.slideshowfx.utils.keys;

import javafx.scene.input.KeyEvent;

/**
 * Provides utilities method for managing key events.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 */
public class KeyEventUtils {

    /**
     * Constant for prefixing a string with {@code Shortcut+}.
     */
    public static String SHORTCUT = "Shortcut+";

    /**
     * Constant for prefixing a string with {@code Shortcut+Shift+}.
     */
    public static String SHORTCUT_SHIFT = "Shortcut+Shift+";

    /**
     * Check if a given {@link KeyEvent} corresponds to the given key combination sequence. A sequence is typically
     * a string like {@code Shortcut+A} or {@code Shortcut+Shift+A}.
     * In order to test the event, this method relies on the {@link KeyTextCombination#valueOf(String)} method.
     * @param value The expected sequence.
     * @param event The event to test.
     * @return {@code true} if the event matches the provided sequence, {@code false} otherwise.
     */
    public static boolean is(final String value, final KeyEvent event) {
        final KeyTextCombination combination = KeyTextCombination.valueOf(value);
        return combination.match(event);
    }

    /**
     * Check if a given {@link KeyEvent} corresponds to the given key combination sequence. A sequence is typically
     * a string like {@code Shortcut+A} or {@code Shortcut+Shift+A}.
     * In order to test the event, this method converts the given text using the {@link #shortcut(String)} method and
     * then call {@link #is(String, KeyEvent)}.
     * @param text The expected text of the sequence.
     * @param event The event to test.
     * @return {@code true} if the event matches the provided sequence, {@code false} otherwise.
     */
    public static boolean isShortcutSequence(final String text, final KeyEvent event) {
        return is(shortcut(text), event);
    }

    /**
     * Utility method that concat the {@link #SHORTCUT} constant with the given text.
     * @param text The text to concat.
     * @return A String that concatenates the {@link #SHORTCUT} constant and the text.
     */
    public static String shortcut(final String text) {
        return SHORTCUT.concat(text);
    }

    /**
     * Utility method that concat the {@link #SHORTCUT_SHIFT} constant with the given text.
     * @param text The text to concat.
     * @return A String that concatenates the {@link #SHORTCUT_SHIFT} constant and the text.
     */
    public static String shortcutShift(final String text) {
        return SHORTCUT_SHIFT.concat(text);
    }
}
