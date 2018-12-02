package com.twasyl.slideshowfx.content.extension.sequence.diagram;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers.SequenceDiagramContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.icons.Icon.SHARE_ALT_SQUARE;

/**
 * The content extension that allows to insert sequence diagrams in a presentation. This extension only supports HTML for
 * inserting the content in the presentation, meaning that HTML code will always be returned when calling
 * {@link #buildDefaultContentString()} and {@link #buildContentString(IMarkup)}.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SequenceDiagramContentExtension extends AbstractContentExtension<SequenceDiagramContentExtensionController> {
    private static final Logger LOGGER = Logger.getLogger(SequenceDiagramContentExtension.class.getName());

    public SequenceDiagramContentExtension() {
        super("SEQUENCE_DIAGRAM",
                SequenceDiagramContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/sequence/diagram/resources/jumly.zip"),
                SHARE_ALT_SQUARE,
                "Insert a sequence diagram",
                "Insert a sequence diagram");

        final String baseURL = "jumly/0.2.3/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/jquery-2.1.0.min.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/coffee-script-1.7.1.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("jumly.min.js"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/sequence/diagram/fxml/SequenceDiagramContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for SequenceDiagramContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder("<script type='text/jumly+sequence'>\n");

        builder.append(this.getController().getSequenceDiagramText())
                .append("\n</script>");

        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }
}
