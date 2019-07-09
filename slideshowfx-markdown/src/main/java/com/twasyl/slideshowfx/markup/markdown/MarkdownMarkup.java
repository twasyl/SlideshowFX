package com.twasyl.slideshowfx.markup.markdown;

import com.github.rjeschke.txtmark.Processor;
import com.twasyl.slideshowfx.markup.AbstractMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;

/**
 * This class implements the Markdown syntax.
 * This markup language is identified byt the code <code>MARKDOWN</code> which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
@Plugin
public class MarkdownMarkup extends AbstractMarkup {

    public MarkdownMarkup() {
        super("MARKDOWN", "Markdown", "ace/mode/markdown");
    }

    /**
     * This methods convert the given <code>markupString</code> to HTML.
     * This method assumes the given String is in the correct mardodwn format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the markdown string.
     * @throws IllegalArgumentException If <code>markupString</code> is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if (markupString == null)
            throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");
        return Processor.process("[$PROFILE$]: extended\n" + markupString).trim();
    }
}
