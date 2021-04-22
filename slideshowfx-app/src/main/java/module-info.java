module slideshowfx.app {
    opens com.twasyl.slideshowfx.app to javafx.graphics, javafx.fxml;
    opens com.twasyl.slideshowfx.controllers to javafx.graphics, javafx.fxml, javafx.web;
    opens com.twasyl.slideshowfx.controls to javafx.fxml;
    opens com.twasyl.slideshowfx.controls.notification to javafx.fxml;
    opens com.twasyl.slideshowfx.controls.outline to javafx.fxml;
    opens com.twasyl.slideshowfx.controls.tree to javafx.fxml;
    opens com.twasyl.slideshowfx.images;

    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires slideshowfx.content.extension;
    requires slideshowfx.engines;
    requires slideshowfx.global.configuration;
    requires slideshowfx.hosting.connector;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.plugin;
    requires slideshowfx.plugin.manager;
    requires slideshowfx.server;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.style;
    requires slideshowfx.utils;
    requires slideshowfx.logs;
    requires slideshowfx.ui.controls;
    requires io.vertx.core;

    uses com.twasyl.slideshowfx.style.theme.Themes;
    uses com.twasyl.slideshowfx.style.Styles;
}