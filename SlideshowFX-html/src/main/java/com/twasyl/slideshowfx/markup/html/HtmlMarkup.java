package com.twasyl.slideshowfx.markup.html;

import com.twasyl.slideshowfx.markup.AbstractMarkup;

/**
 * This class implements the HTML syntax.
 * This markup language is identified byt the code <code>HTML</code> which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 */
public class HtmlMarkup extends AbstractMarkup {

    public HtmlMarkup() { super("HTML", "HTML", "ace/mode/html"); }

    /**
     * This methods convert the given <code>markupString</code> to HTML.
     * This method assumes the given String is in the correct HTML format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the HTML string.
     * @throws IllegalArgumentException If <code>markupString</code> is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        return markupString;
    }
}
