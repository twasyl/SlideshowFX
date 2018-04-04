package com.twasyl.slideshowfx.content.extension.shape;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.shape.controllers.ShapeContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.icons.Icon.STAR;

/**
 * The {@link ShapeContentExtension} extends the {@link AbstractContentExtension}. It allows to build a content
 * containing shapes to insert inside a SlideshowFX presentation.
 * This extension uses SnapSVG.
 * This extension supports HTML markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class ShapeContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(ShapeContentExtension.class.getName());

    protected ShapeContentExtensionController controller;

    public ShapeContentExtension() {
        super("SHAPE",
                ShapeContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/shape/resources/snapsvg.zip"),
                STAR,
                "Insert shapes",
                "Insert shapes");

        final String baseURL = "snapsvg/0.5.1/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("snap.svg-min.js"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/shape/fxml/ShapeContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for ShapeContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.buildDefaultContentString());

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {
        final String id = "sfx" + System.currentTimeMillis();
        final int drawingWidth = this.controller.getDrawingWidth();
        final int drawingHeight = this.controller.getDrawingHeight();
        final String paper = "paper";

        final StringBuilder builder = new StringBuilder("<svg id=\"").append(id).append("\" ")
                .append("width=\"").append(drawingWidth).append("\" ")
                .append("height=\"").append(drawingHeight).append("\"></svg>\n")
                .append("<script>\n\t")
                .append("var ").append(paper).append(" = Snap('#").append(id).append("');\n\t");

        final StringJoiner shapes = new StringJoiner("\n\t");

        this.controller.getShapes().forEach(drawing -> {
            shapes.add(drawing.buildCreatingInstruction(paper));
        });

        builder.append(shapes.toString()).append("\n</script>");

        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.controller.areInputsValid();
    }
}
