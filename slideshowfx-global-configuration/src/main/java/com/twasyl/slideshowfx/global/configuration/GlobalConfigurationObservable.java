package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.global.configuration.events.ProxyHostChangeEvent;
import com.twasyl.slideshowfx.global.configuration.events.ProxyPortChangeEvent;
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

    /**
     * Method notifying all {@link GlobalConfigurationObserver} that a change to the HTTP proxy host used by the application
     * happened.
     *
     * @param forHttps Boolean for indicating if the HTTPS proxy has changed or if it is the HTTP one.
     * @param oldHost The old proxy host.
     * @param newHost  The new proxy host.
     */
    public void notifyProxyHostChanged(final boolean forHttps, final String oldHost, final String newHost) {
        setChanged();
        notifyObservers(new ProxyHostChangeEvent(forHttps, oldHost, newHost));
    }

    /**
     * Method notifying all {@link GlobalConfigurationObserver} that a change to the HTTP proxy port used by the application
     * happened.
     *
     * @param forHttps Boolean for indicating if the HTTPS proxy has changed or if it is the HTTP one.
     * @param oldPort The old proxy port.
     * @param newPort  The new proxy port.
     */
    public void notifyProxyPortChanged(final boolean forHttps, final Integer oldPort, final Integer newPort) {
        setChanged();
        notifyObservers(new ProxyPortChangeEvent(forHttps, oldPort, newPort));
    }
}
