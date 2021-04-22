module slideshowfx.utils {
    exports com.twasyl.slideshowfx.utils;
    exports com.twasyl.slideshowfx.utils.beans;
    exports com.twasyl.slideshowfx.utils.beans.binding;
    exports com.twasyl.slideshowfx.utils.beans.converter;
    exports com.twasyl.slideshowfx.utils.concurrent;
    exports com.twasyl.slideshowfx.utils.concurrent.actions;
    exports com.twasyl.slideshowfx.utils.io;
    exports com.twasyl.slideshowfx.utils.keys;
    exports com.twasyl.slideshowfx.utils.time;

    requires com.fasterxml.jackson.core;
    requires freemarker;
    requires java.logging;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.jsoup;
    requires slideshowfx.global.configuration;
    requires slideshowfx.style;
    requires io.vertx.core;
}