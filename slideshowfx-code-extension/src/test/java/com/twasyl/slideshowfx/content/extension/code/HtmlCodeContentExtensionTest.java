package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.markup.html.HtmlMarkup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage.JAVA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the {@link CodeContentExtension} class using a HTML markup.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class HtmlCodeContentExtensionTest extends BaseCodeContentExtensionTest {

    @BeforeAll
    public static void setUp() {
        extension = new CodeContentExtension();
        markup = new HtmlMarkup();
    }

    @Test
    public void showLines() {
        extension.controller = mockController("private String s;", true, JAVA);
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
        assertTrue(content.startsWith("<pre class=\"language-java line-numbers\"><code class=\"language-java line-numbers\">"));
    }

    @Test
    public void dontShowLines() {
        extension.controller = mockController("private String s;", false, JAVA);
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertFalse(matcher.find());
    }

    @Test
    public void javaLanguage() {
        extension.controller = mockController("private String s;", true, JAVA);
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile(JAVA.getCssClass());
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
    }

    @Test
    public void lineSpecifierCodeMultiLines() {
        extension.controller = mockController("private String s;\n\nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre class=\"" + JAVA.getCssClass() + "\"><code class=\"" + JAVA.getCssClass() + "\">"));
        assertTrue(content.endsWith("</code></pre>"));
    }

    @Test
    public void withoutLanguageAndDontShowLines() {
        extension.controller = mockController("private String s;", false, null);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre><code>"));
        assertTrue(content.endsWith("</code></pre>"));
    }
}
