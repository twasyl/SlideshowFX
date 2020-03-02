module slideshowfx.dropbox.hosting.connector {
    exports com.twasyl.slideshowfx.hosting.connector.dropbox;

    provides com.twasyl.slideshowfx.hosting.connector.IHostingConnector with com.twasyl.slideshowfx.hosting.connector.dropbox.DropboxHostingConnector;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.hosting.connector.dropbox.DropboxHostingConnector;

    requires dropbox.core.sdk;
    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires slideshowfx.engines;
    requires slideshowfx.global.configuration;
    requires slideshowfx.hosting.connector;
}