package com.twasyl.slideshowfx.content.extension.image;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.image.controllers.ImageContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.icons.Icon.PICTURE_ALT;

/**
 * The ImageContentExtension extends the AbstractContentExtension. It allows to build a content containing images to insert
 * inside a SlideshowFX presentation.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.3
 * @since SlideshowFX 1.0
 */
public class ImageContentExtension extends AbstractContentExtension<ImageContentExtensionController> {
    private static final Logger LOGGER = Logger.getLogger(ImageContentExtension.class.getName());

    public ImageContentExtension() {
        super("IMAGE", null,
                PICTURE_ALT,
                "Insert an image",
                "Insert an image");
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/image/fxml/ImageContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for ImageContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();

        if (this.getController().getSelectedFile() != null) {
            if (markup == null || "HTML".equals(markup.getCode())) {
                builder.append(this.buildDefaultContentString());
            } else if ("TEXTILE".equals(markup.getCode())) {
                builder.append(this.buildTextileContentString());
            } else if ("MARKDOWN".equals(markup.getCode())) {
                builder.append(this.buildMarkdownContentString());
            } else {
                builder.append(this.buildDefaultContentString());
            }
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("<img src=\"")
                .append(this.getController().getSelectedFileUrl())
                .append("\" ");

        if (this.getController().hasImageWidth()) {
            builder.append("width=\"").append(this.getController().getImageWidth()).append("\" ");
        }

        if (this.getController().hasImageHeight()) {
            builder.append("height=\"").append(this.getController().getImageHeight()).append("\" ");
        }

        builder.append("/>");

        return builder.toString();
    }

    private String buildTextileContentString() {
        final StringBuilder builder = new StringBuilder("!");

        final boolean hasImageWidth = this.getController().hasImageWidth();
        final boolean hasImageHeight = this.getController().hasImageHeight();

        if (hasImageWidth || hasImageHeight) {
            builder.append("{");

            if (hasImageWidth) {
                builder.append("width: ").append(this.getController().getImageWidth()).append(";");
            }
            if (hasImageHeight) {
                builder.append("height: ").append(this.getController().getImageHeight()).append(";");
            }

            builder.append("}");
        }

        builder.append(this.getController().getSelectedFileUrl()).append("!");

        return builder.toString();
    }

    private String buildMarkdownContentString() {
        final StringBuilder builder = new StringBuilder();

        final boolean hasImageWidth = this.getController().hasImageWidth();
        final boolean hasImageHeight = this.getController().hasImageHeight();

        if (hasImageWidth || hasImageHeight) {
            builder.append(this.buildDefaultContentString());
        } else {
            builder.append("[](").append(this.getController().getSelectedFileUrl()).append(")");
        }

        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }
}
