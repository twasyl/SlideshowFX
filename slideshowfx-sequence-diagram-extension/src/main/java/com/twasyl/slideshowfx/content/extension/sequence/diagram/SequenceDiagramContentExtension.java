package com.twasyl.slideshowfx.content.extension.sequence.diagram;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers.SequenceDiagramContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import javafx.beans.property.ReadOnlyBooleanProperty;

import static com.twasyl.slideshowfx.icons.Icon.SHARE_ALT_SQUARE;

/**
 * The content extension that allows to insert sequence diagrams in a presentation. This extension only supports HTML for
 * inserting the content in the presentation, meaning that HTML code will always be returned when calling
 * {@link #buildDefaultContentString()} and {@link #buildContentString(IMarkup)}.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class SequenceDiagramContentExtension extends AbstractContentExtension<SequenceDiagramContentExtensionController> {

    public SequenceDiagramContentExtension() {
        super("SEQUENCE_DIAGRAM",
                "/com/twasyl/slideshowfx/content/extension/sequence/diagram/fxml/SequenceDiagramContentExtension.fxml",
                "/com/twasyl/slideshowfx/content/extension/sequence/diagram/resources/jumly.zip",
                SHARE_ALT_SQUARE,
                "Insert a sequence diagram", "Insert a sequence diagram");

        final String baseURL = "jumly/0.2.3/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/jquery-2.1.0.min.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/coffee-script-1.7.1.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("jumly.min.js"));
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
