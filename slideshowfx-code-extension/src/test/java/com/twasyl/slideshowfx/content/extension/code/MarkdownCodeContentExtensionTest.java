package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage.JAVA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * This class tests the {@link CodeContentExtension} class using a markdown markup.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class MarkdownCodeContentExtensionTest extends BaseCodeContentExtensionTest {

    @BeforeAll
    public static void setUp() {
        markup = new MarkdownMarkup();
    }

    @Test
    public void showLines() {
        doReturn(mockController("private String s;", true, JAVA, null)).when(extension).getController();
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
        assertTrue(content.startsWith("<pre class=\"language-java line-numbers\"><code class=\"language-java line-numbers\">"));
    }

    @Test
    public void dontShowLines() {
        doReturn(mockController("private String s;", false, JAVA, null)).when(extension).getController();
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertFalse(matcher.find());
    }

    @Test
    public void javaLanguage() {
        doReturn(mockController("private String s;", true, JAVA, null)).when(extension).getController();
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile(JAVA.getCssClass());
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
    }

    @Test
    public void lineSpecifierCodeMultiLines() {
        doReturn(mockController("private String s;\n\nprivate boolean b;", false, JAVA, null)).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("```" + JAVA.getCssClass()));
        assertTrue(content.endsWith("\n```"));
    }

    @Test
    public void withoutLanguageAndDontShowLines() {
        doReturn(mockController("private String s;", false, null, null)).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("```"));
    }

    @Test
    public void withoutLanguageAndHighlightedLine() {
        doReturn(mockController("private String s;", false, null, "1")).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre data-line=\"1\"><code>"));
        assertTrue(content.endsWith("</code></pre>"));
    }

    @Test
    public void withOneHighlightedLine() {
        doReturn(mockController("private String s;\n\nprivate boolean b;", false, JAVA, "2")).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre class=\"" + JAVA.getCssClass() + "\" data-line=\"2\"><code class=\"" + JAVA.getCssClass() + "\">"));
        assertTrue(content.endsWith("</code></pre>"));
    }

    @Test
    public void withMultipleHighlightedLines() {
        doReturn(mockController("private String s;\n\nprivate boolean b;", false, JAVA, "1,2")).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre class=\"" + JAVA.getCssClass() + "\" data-line=\"1,2\"><code class=\"" + JAVA.getCssClass() + "\">"));
        assertTrue(content.endsWith("</code></pre>"));
    }

    @Test
    public void withHighlightedLinesRange() {
        doReturn(mockController("private String s;\n\nprivate boolean b;", false, JAVA, "1-2")).when(extension).getController();
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("<pre class=\"" + JAVA.getCssClass() + "\" data-line=\"1-2\"><code class=\"" + JAVA.getCssClass() + "\">"));
        assertTrue(content.endsWith("</code></pre>"));
    }
}
