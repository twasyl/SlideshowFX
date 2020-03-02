package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.global.configuration.events.ProxyHostChangeEvent;
import com.twasyl.slideshowfx.global.configuration.events.ProxyPortChangeEvent;
import com.twasyl.slideshowfx.global.configuration.events.ThemeChangeEvent;

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

    /**
     * Method called when the HTTP proxy host used by the application has changed.
     *
     * @param forHttps Indicates if the change occurred for HTTPS or not.
     * @param oldHost The old HTTP proxy host.
     * @param newHost The new HTTP proxy host.
     */
    void updateHttpProxyHost(boolean forHttps, String oldHost, String newHost);

    /**
     * Method called when the HTTP proxy port used by the application has changed.
     *
     * @param forHttps Indicates if the change occurred for HTTPS or not.
     * @param oldPort The old HTTP proxy port.
     * @param newPort The new HTTP proxy port.
     */
    void updateHttpProxyPort(boolean forHttps, Integer oldPort, Integer newPort);

    @Override
    default void update(Observable o, Object arg) {
        if (arg instanceof ThemeChangeEvent) {
            final ThemeChangeEvent event = (ThemeChangeEvent) arg;
            this.updateTheme(event.getOldTheme(), event.getNewTheme());
        } else if (arg instanceof ProxyHostChangeEvent) {
            final ProxyHostChangeEvent event = (ProxyHostChangeEvent) arg;
            this.updateHttpProxyHost(event.isForHttps(), event.getOldHost(), event.getNewHost());
        } else if (arg instanceof ProxyPortChangeEvent) {
            final ProxyPortChangeEvent event = (ProxyPortChangeEvent) arg;
            this.updateHttpProxyPort(event.isForHttps(), event.getOldPort(), event.getNewPort());
        }
    }
}
