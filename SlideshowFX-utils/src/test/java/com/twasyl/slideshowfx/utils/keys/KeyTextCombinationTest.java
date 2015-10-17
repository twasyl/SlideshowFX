/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF UP KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.utils.keys;

import org.junit.Test;

import static javafx.scene.input.KeyCombination.ModifierValue;
import static javafx.scene.input.KeyCombination.ModifierValue.DOWN;
import static javafx.scene.input.KeyCombination.ModifierValue.UP;
import static org.junit.Assert.assertEquals;

/**
 * @author Thierry Wasylczenko
 */
public class KeyTextCombinationTest {

    private void assertCombination(final ModifierValue shift, final ModifierValue alt, final ModifierValue control,
                                   final ModifierValue meta, final ModifierValue shortcut, final String text,
                                   final KeyTextCombination combination) {

        assertEquals(shift, combination.getShift());
        assertEquals(alt, combination.getAlt());
        assertEquals(control, combination.getControl());
        assertEquals(meta, combination.getMeta());
        assertEquals(shortcut, combination.getShortcut());
        assertEquals(text, combination.getText());
    }

    @Test
    public void testShortcutA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Shortcut+A");
        assertCombination(UP, UP, UP, UP, DOWN, "A", combination);
    }

    @Test
    public void testShiftA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Shift+A");
        assertCombination(DOWN, UP, UP, UP, UP, "A", combination);
    }

    @Test
    public void testControlA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Ctrl+A");
        assertCombination(UP, UP, DOWN, UP, UP, "A", combination);
    }

    @Test
    public void testMetaA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Meta+A");
        assertCombination(UP, UP, UP, DOWN, UP, "A", combination);
    }

    @Test
    public void testAltA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Alt+A");
        assertCombination(UP, DOWN, UP, UP, UP, "A", combination);
    }

    @Test
    public void testShortcutMetaA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Shortcut+Meta+A");
        assertCombination(UP, UP, UP, DOWN, DOWN, "A", combination);
    }

    @Test
    public void testMetaShortcutA() {
        final KeyTextCombination combination = KeyTextCombination.valueOf("Meta+Shortcut+A");
        assertCombination(UP, UP, UP, DOWN, DOWN, "A", combination);
    }
}
