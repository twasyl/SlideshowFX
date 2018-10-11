package com.twasyl.slideshowfx.theme;

public class ThemeNotFoundException extends RuntimeException {

    public ThemeNotFoundException(final String name) {
        super("Theme '" + name + "' not found");
    }
}
