module slideshowfx.hosting.connector {
    exports com.twasyl.slideshowfx.hosting.connector;
    exports com.twasyl.slideshowfx.hosting.connector.exceptions;
    exports com.twasyl.slideshowfx.hosting.connector.io;

    requires java.logging;
    requires javafx.controls;
    requires slideshowfx.engines;
    requires transitive slideshowfx.plugin;
    requires slideshowfx.utils;
}