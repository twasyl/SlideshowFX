module slideshowfx.engines {
    exports com.twasyl.slideshowfx.engine;
    exports com.twasyl.slideshowfx.engine.presentation;
    exports com.twasyl.slideshowfx.engine.presentation.configuration;
    exports com.twasyl.slideshowfx.engine.template;
    exports com.twasyl.slideshowfx.engine.template.configuration;

    opens com.twasyl.slideshowfx.engine to javafx.base;

    requires freemarker;
    requires java.desktop;
    requires java.logging;
    requires javafx.graphics;
    requires javafx.swing;
    requires org.jsoup;
    requires slideshowfx.content.extension;
    requires slideshowfx.global.configuration;
    requires slideshowfx.utils;
    requires io.vertx.core;
}