package com.twasyl.slideshowfx.global.configuration.events;

/**
 * Event representing a theme change.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ThemeChangeEvent {
    private String oldTheme;
    private String newTheme;

    public ThemeChangeEvent(String oldTheme, String newTheme) {
        this.oldTheme = oldTheme;
        this.newTheme = newTheme;
    }

    public String getOldTheme() {
        return oldTheme;
    }

    public String getNewTheme() {
        return newTheme;
    }
}
