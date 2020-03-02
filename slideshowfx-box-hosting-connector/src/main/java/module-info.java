module slideshowfx.box.hosting.connector {
    exports com.twasyl.slideshowfx.hosting.connector.box;

    provides com.twasyl.slideshowfx.hosting.connector.IHostingConnector with com.twasyl.slideshowfx.hosting.connector.box.BoxHostingConnector;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.hosting.connector.box.BoxHostingConnector;

    requires box.java.sdk;
    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires slideshowfx.engines;
    requires slideshowfx.global.configuration;
    requires slideshowfx.hosting.connector;
}