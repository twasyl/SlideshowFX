package com.twasyl.slideshowfx.markup.markdown;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import com.twasyl.slideshowfx.plugin.Plugin;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

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
    private final List<Extension> extensions;

    public MarkdownMarkup() {
        super("MARKDOWN", "Markdown", "ace/mode/markdown");
        extensions = List.of(TablesExtension.create());
    }

    /**
     * This methods convert the given <code>markupString</code> to HTML.
     * This method assumes the given String is in the correct markdown format.
     *
     * @param markupString The string written in the markup syntax to convert as HTML.
     * @return the HTML representation of the markdown string.
     * @throws IllegalArgumentException If <code>markupString</code> is null, this exception is thrown.
     */
    @Override
    public String convertAsHtml(String markupString) {
        if (markupString == null)
            throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        final Parser parser = Parser.builder().extensions(extensions).build();
        final Node node = parser.parse(markupString);
        final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        return renderer.render(node).trim();
    }
}
