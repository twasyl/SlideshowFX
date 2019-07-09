package com.twasyl.slideshowfx.content.extension.image;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.image.controllers.ImageContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import javafx.beans.property.ReadOnlyBooleanProperty;

import static com.twasyl.slideshowfx.icons.Icon.PICTURE_ALT;

/**
 * The ImageContentExtension extends the AbstractContentExtension. It allows to build a content containing images to insert
 * inside a SlideshowFX presentation.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.4-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class ImageContentExtension extends AbstractContentExtension<ImageContentExtensionController> {

    public ImageContentExtension() {
        super("IMAGE",
                "/com/twasyl/slideshowfx/content/extension/image/fxml/ImageContentExtension.fxml",
                null,
                PICTURE_ALT,
                "Insert an image", "Insert an image");
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
