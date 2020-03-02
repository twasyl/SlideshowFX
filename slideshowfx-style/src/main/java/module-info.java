module slideshowfx.style {
    exports com.twasyl.slideshowfx.style;
    exports com.twasyl.slideshowfx.style.theme;

    opens com.twasyl.slideshowfx.style.css;
    opens com.twasyl.slideshowfx.style.images;
    opens com.twasyl.slideshowfx.style.theme.css;

    requires java.logging;
    requires java.xml;
    requires javafx.graphics;
}