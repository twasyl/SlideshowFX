module slideshowfx.content.extension {
    exports com.twasyl.slideshowfx.content.extension;

    requires java.logging;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires transitive slideshowfx.plugin;
    requires slideshowfx.utils;
}