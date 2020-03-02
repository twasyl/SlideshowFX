module slideshowfx.snippet.extension {
    opens com.twasyl.slideshowfx.content.extension.snippet.controllers to javafx.fxml, javafx.graphics;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.snippet.SnippetContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.snippet.SnippetContentExtension;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.global.configuration;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.plugin.manager;
    requires slideshowfx.snippet.executor;
    requires slideshowfx.ui.controls;
}