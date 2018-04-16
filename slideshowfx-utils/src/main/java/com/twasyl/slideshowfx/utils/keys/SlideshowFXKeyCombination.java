package com.twasyl.slideshowfx.utils.keys;

import com.twasyl.slideshowfx.utils.OSUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows to create a {@link KeyCombination} that matches the text or the {@link KeyCode} of an
 * {@link KeyEvent}.
 * Whether the text or the {@link KeyCode} is checked depends on the platform: on OSX the text is checked while the
 * {@link KeyCode} is checked on Windows and Linux systems.
 * The platform is determined using {@link OSUtils#isMac()}, {@link OSUtils#isWindows()} and
 * {@link OSUtils#isLinux()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 */
public class SlideshowFXKeyCombination extends KeyCombination {
    public static final Logger LOGGER = Logger.getLogger(SlideshowFXKeyCombination.class.getName());
    private final String text;
    private final KeyCode code;

    public SlideshowFXKeyCombination(final String text, final KeyCode code, final ModifierValue shift, final ModifierValue control,
                                     final ModifierValue alt, final ModifierValue meta, final ModifierValue shortcut) {
        super(shift, control, alt, meta, shortcut);
        this.text = text;
        this.code = code;
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
        LOGGER.log(Level.FINE, String.format("event.getText(): %1$s", event.getText()));
        LOGGER.log(Level.FINE, String.format("event.getCharacter(): %1$s", event.getCharacter()));
        LOGGER.log(Level.FINE, String.format("event.getCode(): %1$s", event.getCode().name()));

        boolean match = super.match(event);

        if (match) {
            // Depending on the platform, we have either to check the text of the event, either the code.
            if (OSUtils.isMac()) {
                match = text.equalsIgnoreCase(event.getText());
            } else if (OSUtils.isLinux() || OSUtils.isWindows()) {
                match = code == event.getCode();
            }
        }

        return match;
    }

    /**
     * Converts a given {@link String} into a {@link KeyCombination} that matches the text or the code of an event.
     * Whether the code or the text is checked depends on the platform: on OSX, the text will be tested while on
     * other platform the code will be checked.
     *
     * @param value The value to parse.
     * @return A {@link SlideshowFXKeyCombination} parsed from a string.
     * @throws NullPointerException     If the provided value is {@code null}.
     * @throws IllegalArgumentException If the provided value is empty.
     */
    public static SlideshowFXKeyCombination valueOf(String value) {
        if (value == null) throw new NullPointerException("The value can not be null");
        if (value.isEmpty()) throw new IllegalArgumentException("The value can not be empty");

        String determinedText = null;
        KeyCode determinedCode = KeyCode.UNDEFINED;

        ModifierValue shiftModifier = ModifierValue.UP;
        ModifierValue controlModifier = ModifierValue.UP;
        ModifierValue shortcutModifier = ModifierValue.UP;
        ModifierValue metaModifier = ModifierValue.UP;
        ModifierValue altModifier = ModifierValue.UP;

        final String[] tokens = value.split("\\+");
        for (String token : tokens) {
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
                    determinedText = token;
                    determinedCode = KeyCode.valueOf(determinedText);
            }
        }
        final SlideshowFXKeyCombination combination = new SlideshowFXKeyCombination(determinedText, determinedCode, shiftModifier,
                controlModifier, altModifier, metaModifier, shortcutModifier);

        return combination;
    }

    /**
     * Tests whether this {@code SlideshowFXKeyCombination} equals to the
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

        if (!(obj instanceof SlideshowFXKeyCombination)) {
            return false;
        }

        return this.text.equals(((SlideshowFXKeyCombination) obj).getText())
                && this.code.equals(((SlideshowFXKeyCombination) obj).code)
                && super.equals(obj);
    }

    /**
     * Returns a hash code value for this {@code SlideshowFXKeyCombination}.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return 24 * super.hashCode() + this.text.hashCode() + this.code.hashCode();
    }
}
