module slideshowfx.quiz.extension {
    opens com.twasyl.slideshowfx.content.extension.quiz.controllers to javafx.fxml, javafx.graphics;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.quiz.QuizContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.quiz.QuizContentExtension;

    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.global.configuration;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.server;
    requires slideshowfx.ui.controls;
}