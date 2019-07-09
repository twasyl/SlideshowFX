package com.twasyl.slideshowfx.content.extension.quote;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.quote.controllers.QuoteContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import javafx.beans.property.ReadOnlyBooleanProperty;

import static com.twasyl.slideshowfx.icons.Icon.QUOTE_LEFT;

/**
 * The QuoteContentExtension extends the AbstractContentExtension. It allows to build a content containing quote to insert
 * inside a SlideshowFX presentation.
 * This extension doesn't use other resources
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class QuoteContentExtension extends AbstractContentExtension<QuoteContentExtensionController> {

    public QuoteContentExtension() {
        super("QUOTE",
                "/com/twasyl/slideshowfx/content/extension/quote/fxml/QuoteContentExtension.fxml",
                null,
                QUOTE_LEFT,
                "Insert a quote", "Insert a quote");
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();

        if (markup == null || "HTML".equals(markup.getCode())) {
            builder.append(this.buildDefaultContentString());
        } else if ("TEXTILE".equals(markup.getCode())) {
            builder.append("bq.. ")
                    .append(this.getController().getQuote())
                    .append("\np{text-align: right; font-weight: bold; font-style: italic;}. ")
                    .append(this.getController().getAuthor());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("<blockquote><p>")
                .append(this.getController().getQuote())
                .append("</p></blockquote>\n<p style=\"text-align: right; font-weight: bold; font-style: italic;\">")
                .append(this.getController().getAuthor())
                .append("</p>");

        return builder.toString();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        return this.getController().areInputsValid();
    }
}
