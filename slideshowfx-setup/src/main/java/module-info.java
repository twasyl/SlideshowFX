module slideshowfx.setup {
    opens com.twasyl.slideshowfx.setup.app to javafx.graphics;
    opens com.twasyl.slideshowfx.setup.controllers to javafx.fxml;
    opens com.twasyl.slideshowfx.setup.css;
    opens com.twasyl.slideshowfx.setup.images;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.global.configuration;
    requires slideshowfx.icons;
    requires slideshowfx.plugin.manager;
    requires slideshowfx.ui.controls;
    requires slideshowfx.utils;
}