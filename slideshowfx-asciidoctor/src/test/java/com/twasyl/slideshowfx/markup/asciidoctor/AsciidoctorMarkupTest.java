package com.twasyl.slideshowfx.markup.asciidoctor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Performs tests for the Asciidoctor markup syntax.
 *
 * @author Thierry Wasylczenko
 */
@Disabled
public class AsciidoctorMarkupTest {

    private static AsciidoctorMarkup markup;

    @BeforeAll
    public static void setUp() {
        markup = new AsciidoctorMarkup();
    }

    @Test
    public void generateWithNull() {
        assertThrows(IllegalArgumentException.class, () -> markup.convertAsHtml(null));
    }

    @Test public void generateH1() {
        final String result = markup.convertAsHtml("= A title");

        assertEquals("<h1>A title</h1>", result);
    }

    @Test public void generateH2() {
        final String result = markup.convertAsHtml("== A title");
        System.out.println(result);
        assertEquals("<h2>A title</h2>", result);
    }

    @Test public void generateInlineCode() {
        final String result = markup.convertAsHtml("<code>public class Java { }</code>");

        assertEquals("<code>public class Java { }</code>", result);
    }

    @Test public void generateCodeBloc() {
        final String result = markup.convertAsHtml("[source,java]\n----\nfinal String s;\n----\n");
        System.out.println(result);
        assertEquals("<pre><code>final String s;</code></pre>", result);
    }

    @Test public void generateStrong() {
        final String result = markup.convertAsHtml("*Strong text*");

        assertEquals("<strong>Strong text</strong>", result);
    }

    @Test public void generateUnorderedList() {
        final String result = markup.convertAsHtml("<ul><li>One</li><li>Two</li></ul>");

        assertEquals("<ul><li>One</li><li>Two</li></ul>", result);
    }
}
