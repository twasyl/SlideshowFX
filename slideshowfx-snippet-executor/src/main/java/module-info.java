module slideshowfx.snippet.executor {
    exports com.twasyl.slideshowfx.snippet.executor;

    requires java.logging;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires transitive slideshowfx.plugin;
    requires io.vertx.core;
}