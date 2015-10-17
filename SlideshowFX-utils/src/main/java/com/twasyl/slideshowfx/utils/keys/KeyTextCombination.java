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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.utils.keys;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * This class allows to create a {@link KeyCombination} that matches the text of a {@link KeyEvent}.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 */
public class KeyTextCombination extends KeyCombination {
    private final String text;

    public KeyTextCombination(String text, ModifierValue shift, ModifierValue control, ModifierValue alt,
                              ModifierValue meta, ModifierValue shortcut) {
        super(shift, control, alt, meta, shortcut);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getDisplayText() {
        return super.getDisplayText().concat(this.text);
    }

    @Override
    public boolean match(KeyEvent event) {
        return super.match(event) && text.equalsIgnoreCase(event.getText());
    }

    /**
     * Converts a given {@link String} into a {@link KeyCombination} that matches the text of an event.
     * @param value The value to parse.
     * @return A {@link KeyTextCombination} parsed from a string.
     * @throws NullPointerException If the provided value is {@code null}.
     * @throws IllegalArgumentException If the provided value is empty.
     */
    public static KeyTextCombination valueOf(String value) {
        if(value == null) throw new NullPointerException("The value can not be null");
        if(value.isEmpty()) throw new IllegalArgumentException("The value can not be empty");

        String providedText = null;

        ModifierValue shiftModifier = ModifierValue.UP;
        ModifierValue controlModifier = ModifierValue.UP;
        ModifierValue shortcutModifier = ModifierValue.UP;
        ModifierValue metaModifier = ModifierValue.UP;
        ModifierValue altModifier = ModifierValue.UP;

        final String[] tokens = value.split("\\+");
        for(String token : tokens) {
            switch (token) {
                case "Shortcut":
                    shortcutModifier = ModifierValue.DOWN;
                    break;
                case "Ctrl":
                    controlModifier = ModifierValue.DOWN;
                    break;
                case "Shift":
                    shiftModifier = ModifierValue.DOWN;
                    break;
                case "Meta":
                    metaModifier = ModifierValue.DOWN;
                    break;
                case "Alt":
                    altModifier = ModifierValue.DOWN;
                    break;
                default:
                    providedText = token;
            }
        }
        final KeyTextCombination combination  = new KeyTextCombination(providedText, shiftModifier, controlModifier,
                altModifier, metaModifier, shortcutModifier);


        return combination;
    }

    /**
     * Tests whether this {@code KeyTextCombination} equals to the
     * specified object.
     *
     * @param obj the object to compare to
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof KeyTextCombination)) {
            return false;
        }

        return this.text.equals(((KeyTextCombination) obj).getText())
                && super.equals(obj);
    }

    /**
     * Returns a hash code value for this {@code KeyTextCombination}.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return 24 * super.hashCode() + text.hashCode();
    }
}
