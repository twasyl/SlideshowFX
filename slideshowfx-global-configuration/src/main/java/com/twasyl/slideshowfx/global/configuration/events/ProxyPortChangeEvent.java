package com.twasyl.slideshowfx.global.configuration.events;

/**
 * Event representing a HTTP proxy port change.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ProxyPortChangeEvent {
    private boolean forHttps;
    private Integer oldPort;
    private Integer newPort;

    public ProxyPortChangeEvent(boolean forHttps, Integer oldPort, Integer newPort) {
        this.forHttps = forHttps;
        this.oldPort = oldPort;
        this.newPort = newPort;
    }

    public boolean isForHttps() {
        return forHttps;
    }

    public Integer getOldPort() {
        return oldPort;
    }

    public Integer getNewPort() {
        return newPort;
    }
}
