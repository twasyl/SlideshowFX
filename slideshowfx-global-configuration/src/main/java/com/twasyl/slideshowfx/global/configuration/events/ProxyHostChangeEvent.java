package com.twasyl.slideshowfx.global.configuration.events;

/**
 * Event representing a HTTP proxy host change.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ProxyHostChangeEvent {
    private boolean forHttps;
    private String oldHost;
    private String newHost;

    public ProxyHostChangeEvent(boolean forHttps, String oldHost, String newHost) {
        this.forHttps = forHttps;
        this.oldHost = oldHost;
        this.newHost = newHost;
    }

    public boolean isForHttps() {
        return forHttps;
    }

    public String getOldHost() {
        return oldHost;
    }

    public String getNewHost() {
        return newHost;
    }
}
