module slideshowfx.code.extension {
    opens com.twasyl.slideshowfx.content.extension.code.controllers to javafx.graphics, javafx.fxml;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.code.CodeContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.code.CodeContentExtension;

    requires javafx.controls;
    requires javafx.graphics;
    requires java.logging;
    requires javafx.fxml;
    requires slideshowfx.content.extension;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.ui.controls;
}