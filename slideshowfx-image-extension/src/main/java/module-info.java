module slideshowfx.image.extension {
    opens com.twasyl.slideshowfx.content.extension.image.controllers to javafx.fxml, javafx.graphics;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.image.ImageContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.image.ImageContentExtension;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.plugin.manager;
    requires slideshowfx.utils;
}