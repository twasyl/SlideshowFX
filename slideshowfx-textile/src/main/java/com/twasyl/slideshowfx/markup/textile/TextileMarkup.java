package com.twasyl.slideshowfx.markup.textile;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.textile.TextileLanguage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Textile syntax.
 * This markup language is identified byt the code {@code TEXTILE} which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class TextileMarkup extends AbstractMarkup {
    private static final Logger LOGGER = Logger.getLogger(TextileMarkup.class.getName());

    public TextileMarkup() {
        super("TEXTILE", "Textile", "ace/mode/textile");
    }

    /**
     * This methods convert the given {@code markupString} to HTML.
     * This method assumes the given String is in the correct textile format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the textile string.
     * @throws IllegalArgumentException If {@code markupString} is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) {
        if (markupString == null)
            throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        String result = null;

        try (final StringWriter writer = new StringWriter()) {
            final DocumentBuilder builder = new HtmlDocumentBuilder(writer);
            final MarkupParser parser = new MarkupParser(new TextileLanguage(), builder);

            parser.parse(markupString, false);
            builder.flush();
            writer.flush();

            result = writer.toString();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while converting to textile");
        }

        return result;
    }
}
