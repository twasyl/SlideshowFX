module slideshowfx.server {
    exports com.twasyl.slideshowfx.server;
    exports com.twasyl.slideshowfx.server.service;
    exports com.twasyl.slideshowfx.server.bus;
    exports com.twasyl.slideshowfx.server.beans.quiz;
    exports com.twasyl.slideshowfx.server.beans.chat;

    opens com.twasyl.slideshowfx.server.webapp.css;
    opens com.twasyl.slideshowfx.server.webapp.html;
    opens com.twasyl.slideshowfx.server.webapp.images;
    opens com.twasyl.slideshowfx.server.webapp.js;

    requires freemarker;
    requires java.logging;
    requires java.xml;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.xml.dom;
    requires slideshowfx.global.configuration;
    requires slideshowfx.icons;
    requires slideshowfx.utils;
    requires vertx.core;
    requires vertx.web;
}