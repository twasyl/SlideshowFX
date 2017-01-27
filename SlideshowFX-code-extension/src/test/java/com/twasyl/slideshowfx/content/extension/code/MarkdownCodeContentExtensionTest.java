package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage.JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link CodeContentExtension} class using a markdown markup.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class MarkdownCodeContentExtensionTest extends BaseCodeContentExtensionTest {

    @BeforeClass
    public static void setUp() {
        extension = new CodeContentExtension();
        markup = new MarkdownMarkup();
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

        assertTrue(content.startsWith("```" + JAVA.getCssClass()));
        assertTrue(content.endsWith("\n```"));
    }

    @Test
    public void withoutLanguageAndDontShowLines() {
        extension.controller = mockController("private String s;", false, null);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("```"));
    }
}
