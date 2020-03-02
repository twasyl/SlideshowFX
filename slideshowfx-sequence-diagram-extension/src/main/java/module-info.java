module slideshowfx.sequence.diagram.extension {
    opens com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers to javafx.fxml, javafx.graphics;

    provides com.twasyl.slideshowfx.content.extension.IContentExtension with com.twasyl.slideshowfx.content.extension.sequence.diagram.SequenceDiagramContentExtension;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.content.extension.sequence.diagram.SequenceDiagramContentExtension;

    requires java.desktop;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires slideshowfx.content.extension;
    requires slideshowfx.icons;
    requires slideshowfx.markup;
    requires slideshowfx.ui.controls;
}