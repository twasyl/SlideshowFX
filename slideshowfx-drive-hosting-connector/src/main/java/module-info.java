module slideshowfx.drive.hosting.connector {
    exports com.twasyl.slideshowfx.hosting.connector.drive;

    provides com.twasyl.slideshowfx.hosting.connector.IHostingConnector with com.twasyl.slideshowfx.hosting.connector.drive.DriveHostingConnector;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.hosting.connector.drive.DriveHostingConnector;

    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.services.drive;
    requires google.api.client;
    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires slideshowfx.engines;
    requires slideshowfx.global.configuration;
    requires slideshowfx.hosting.connector;
}