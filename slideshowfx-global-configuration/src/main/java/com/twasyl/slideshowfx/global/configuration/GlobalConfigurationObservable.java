package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.global.configuration.events.ThemeChangeEvent;

import java.util.Observable;
import java.util.Observer;

/**
 * Class defining an {@link Observable} for firing events concerning the global configuration of the application.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class GlobalConfigurationObservable extends Observable {

    @Override
    public synchronized void addObserver(Observer o) {
        if (o instanceof GlobalConfigurationObserver) {
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
