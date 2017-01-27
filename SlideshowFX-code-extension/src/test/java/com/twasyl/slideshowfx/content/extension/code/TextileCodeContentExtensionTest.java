package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage.JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * This class tests the {@link CodeContentExtension} class.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.1
 */
public class TextileCodeContentExtensionTest extends BaseCodeContentExtensionTest {

    @BeforeClass
    public static void setUp() {
        extension = new CodeContentExtension();
        markup = new TextileMarkup();
    }

    @Test
    public void showLines() {
        extension.controller = mockController("private String s;", true, JAVA);
        final String content = extension.buildContentString(markup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
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
    public void lineSpecifierCodeMultiLineWithoutEmptyLines() {
        extension.controller = mockController("private String s;\nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + "). "));
    }

    @Test
    public void lineSpecifierCodeMultiLineWithEmptyLines() {
        extension.controller = mockController("private String s;\n\nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + ").. "));
    }

    @Test
    public void lineSpecifierCodeMultiLineWithLinesContainingOnlySpaces() {
        extension.controller = mockController("private String s;\n  \nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + ").. "));
    }

    @Test
    public void lineSpecifierCodeSingleLineWithoutEmptyLines() {
        extension.controller = mockController("private String s;", false, JAVA);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + "). "));
    }

    @Test
    public void withoutLanguageAndDontShowLines() {
        extension.controller = mockController("private String s;", false, null);
        final String content = extension.buildContentString(markup);

        assertTrue(content.startsWith("bc. "));
    }
}
