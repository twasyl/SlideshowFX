package com.twasyl.slideshowfx.global.configuration;

import java.util.Observable;
import java.util.Observer;

/**
 * Class defining an {@link Observable} for firing events concerning the global configuration of the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class GlobalConfigurationObservable extends Observable {
    protected static class ThemeChangeEvent {
        private String oldTheme, newTheme;

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

    @Override
    public synchronized void addObserver(Observer o) {
        if (o != null && o instanceof GlobalConfigurationObserver) {
            super.addObserver(o);
        }
    }

    /**
     * Method notifying all {@link GlobalConfigurationObserver} that a change to the theme used by the application
     * happened.
     *
     * @param oldTheme The old theme name.
     * @param newTheme The new theme name.
     */
    public void notifyThemeChanged(final String oldTheme, final String newTheme) {
        setChanged();
        notifyObservers(new ThemeChangeEvent(oldTheme, newTheme));
    }
}
