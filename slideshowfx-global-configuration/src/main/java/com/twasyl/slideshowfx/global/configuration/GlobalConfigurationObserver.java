package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.global.configuration.GlobalConfigurationObservable.ThemeChangeEvent;

import java.util.Observable;
import java.util.Observer;

/**
 * Interface defining an {@link Observer} listening to changes to the GlobalConfiguration.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public interface GlobalConfigurationObserver extends Observer {

    /**
     * Method called when the theme used by the application has changed.
     *
     * @param oldTheme The old theme name.
     * @param newTheme The new theme name.
     */
    void updateTheme(String oldTheme, String newTheme);

    @Override
    default void update(Observable o, Object arg) {
        if (arg != null && arg instanceof ThemeChangeEvent) {
            ThemeChangeEvent event = (ThemeChangeEvent) arg;
            this.updateTheme(event.getOldTheme(), event.getNewTheme());
        }
    }
}
