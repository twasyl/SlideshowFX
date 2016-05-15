package com.twasyl.slideshowfx.markup.textile;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import org.eclipse.mylyn.internal.wikitext.textile.core.TextileContentState;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.IdGenerator;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.ContentState;
import org.eclipse.mylyn.wikitext.core.parser.markup.IdGenerationStrategy;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Textile syntax.
 * This markup language is identified byt the code {@code TEXTILE} which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class TextileMarkup extends AbstractMarkup {
    private static final Logger LOGGER = Logger.getLogger(TextileMarkup.class.getName());

    // The generation strategy generates IDs using the current timestamp
    final IdGenerationStrategy idGenerationStrategy = new IdGenerationStrategy() {
        @Override
        public String generateId(String s) {
            return System.currentTimeMillis() + "";
        }
    };

    final IdGenerator idGenerator = new IdGenerator();

    final TextileContentState contentState = new TextileContentState() {
        @Override
        public IdGenerator getIdGenerator() {
            return idGenerator;
        }
    };

    // Override the language to return the created contentState used for the ID generation
    final MarkupLanguage language = new TextileLanguage() {
        @Override
        protected ContentState createState() {
            return contentState;
        }
    };

    public TextileMarkup() {
        super("TEXTILE", "Textile", "ace/mode/textile");
        idGenerator.setGenerationStrategy(idGenerationStrategy);
    }

    /**
     * This methods convert the given {@code >markupString} to HTML.
     * This method assumes the given String is in the correct textile format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the textile string.
     * @throws IllegalArgumentException If {@code >markupString} is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        String result = null;

        try(final StringWriter writer  = new StringWriter()) {
            final DocumentBuilder builder = new HtmlDocumentBuilder(writer);
            final MarkupParser parser = new MarkupParser(language, builder);

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
