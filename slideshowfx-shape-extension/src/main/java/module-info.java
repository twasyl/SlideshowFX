module slideshowfx.shape.extension {
    opens com.twasyl.slideshowfx.content.extension.shape.controllers to javafx.fxml, javafx.graphics;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.shape.ShapeContentExtension;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.ui.controls;
    requires slideshowfx.utils;
}