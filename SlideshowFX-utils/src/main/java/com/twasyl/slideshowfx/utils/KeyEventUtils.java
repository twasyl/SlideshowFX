package com.twasyl.slideshowfx.utils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;

/**
 * Provides utilities method for managing key events.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 */
public class KeyEventUtils {

    /**
     * Check if a given {@link KeyEvent} corresponds to the given {@link KeyCode}. This methods checks the text as well
     * as the code of the event.
     * @param code The expected code.
     * @param event The event to test.
     * @return {@code true} if the event is of the desired code, {@code false} otherwise.
     */
    public static boolean is(final KeyCode code, final KeyEvent event) {
        if(code == null) throw new NullPointerException("The code can not be null");
        if(event == null) throw new NullPointerException("The event can not be null");


        final KeyCodeCombination codeCombination = new KeyCodeCombination(code,
                event.isShiftDown() ? ModifierValue.DOWN : ModifierValue.UP,
                event.isControlDown() ? ModifierValue.DOWN : ModifierValue.UP,
                event.isAltDown() ? ModifierValue.DOWN : ModifierValue.UP,
                event.isMetaDown() ? ModifierValue.DOWN : ModifierValue.UP,
                event.isShortcutDown() ? ModifierValue.DOWN : ModifierValue.UP);

        return codeCombination.match(event);
    }
}
