package com.twasyl.slideshowfx.markup.textile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thierry Wasylczenko
 */
public class TextileMarkupTest {

    private static TextileMarkup markup;

    @BeforeAll
    public static void setUp() {
        markup = new TextileMarkup();
    }

    @Test
    public void generateWithNull() {
        assertThrows(IllegalArgumentException.class, () -> markup.convertAsHtml(null));
    }

    @Test
    public void generateH1() {
        final String result = markup.convertAsHtml("h1. A title");

        assertEquals("<h1 id=\"Atitle\">A title</h1>", result);
    }

    @Test
    public void generateH2() {
        final String result = markup.convertAsHtml("h2. A title");

        assertEquals("<h2 id=\"Atitle\">A title</h2>", result);
    }

    @Test
    public void generateInlineCode() {
        final String result = markup.convertAsHtml("@public class Java { }@");

        assertEquals("<p><code>public class Java { }</code></p>", result);
    }

    @Test
    public void generateCodeBloc() {
        final String result = markup.convertAsHtml("bc. final String s;");

        assertEquals("<pre><code>final String s;\n</code></pre>", result);
    }

    @Test
    public void generateStrong() {
        final String result = markup.convertAsHtml("*Strong text*");

        assertEquals("<p><strong>Strong text</strong></p>", result);
    }

    @Test
    public void generateUnorderedList() {
        final String result = markup.convertAsHtml("* One\n* Two");

        assertEquals("<ul><li>One</li><li>Two</li></ul>", result);
    }

    @Test
    public void generateTable() {
        final String result = markup.convertAsHtml(
                        """
                        |_. Column 1 |_. Column 2 |
                        | Value 1 | Value 2 |""");

        assertEquals("<table><tr><th>Column 1</th><th>Column 2</th></tr><tr><td>Value 1</td><td>Value 2</td></tr></table>", result);
    }
}
