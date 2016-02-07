package com.twasyl.slideshowfx.utils.keys;

import org.junit.Test;

import static javafx.scene.input.KeyCombination.ModifierValue;
import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;
import static org.junit.Assert.assertEquals;

/**
 * @author Thierry Wasylczenko
 */
public class SlideshowFXKeyCombinationTest {

    private void assertCombination(final ModifierValue shift, final ModifierValue alt, final ModifierValue control,
                                   final ModifierValue meta, final ModifierValue shortcut, final String text,
                                   final SlideshowFXKeyCombination combination) {

        assertEquals(shift, combination.getShift());
        assertEquals(alt, combination.getAlt());
        assertEquals(control, combination.getControl());
        assertEquals(meta, combination.getMeta());
        assertEquals(shortcut, combination.getShortcut());
        assertEquals(text, combination.getText());
    }

    @Test
    public void testShortcutA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Shortcut+A");
        assertCombination(UP, UP, UP, UP, DOWN, "A", combination);
    }

    @Test
    public void testShiftA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Shift+A");
        assertCombination(DOWN, UP, UP, UP, UP, "A", combination);
    }

    @Test
    public void testControlA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Ctrl+A");
        assertCombination(UP, UP, DOWN, UP, UP, "A", combination);
    }

    @Test
    public void testMetaA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Meta+A");
        assertCombination(UP, UP, UP, DOWN, UP, "A", combination);
    }

    @Test
    public void testAltA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Alt+A");
        assertCombination(UP, DOWN, UP, UP, UP, "A", combination);
    }

    @Test
    public void testShortcutMetaA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Shortcut+Meta+A");
        assertCombination(UP, UP, UP, DOWN, DOWN, "A", combination);
    }

    @Test
    public void testMetaShortcutA() {
        final SlideshowFXKeyCombination combination = SlideshowFXKeyCombination.valueOf("Meta+Shortcut+A");
        assertCombination(UP, UP, UP, DOWN, DOWN, "A", combination);
    }
}
