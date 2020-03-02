module slideshowfx.alert.extension {
    exports com.twasyl.slideshowfx.content.extension.alert.controllers;

    opens com.twasyl.slideshowfx.content.extension.alert.controllers to javafx.graphics, javafx.fxml;
    opens com.twasyl.slideshowfx.content.extension.alert.resources;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.alert.AlertContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.alert.AlertContentExtension;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.ui.controls;
}