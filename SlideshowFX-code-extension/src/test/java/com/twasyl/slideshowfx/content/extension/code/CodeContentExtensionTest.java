package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage.JAVA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link CodeContentExtension} class.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class CodeContentExtensionTest {

    private static CodeContentExtension extension;
    private static IMarkup textileMarkup;

    @BeforeClass
    public static void setUp() {
        extension = new CodeContentExtension();
        textileMarkup = new TextileMarkup();
    }

    private CodeContentExtensionController mockController(final String code, final boolean showLineNumbers, final SupportedLanguage language) {
        final CodeContentExtensionController controller = mock(CodeContentExtensionController.class);
        when(controller.getCode()).thenReturn(code);
        when(controller.isShowingLineNumbers()).thenReturn(showLineNumbers);
        when(controller.getLanguage()).thenReturn(language);

        return controller;
    }

    @Test
    public void textileShowLines() {
        extension.controller = mockController("private String s;", true, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
    }

    @Test
    public void textileDontShowLines() {
        extension.controller = mockController("private String s;", false, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        final Pattern pattern = Pattern.compile("line\\-numbers");
        final Matcher matcher = pattern.matcher(content);

        assertFalse(matcher.find());
    }

    @Test
    public void textileTestJavaLanguage() {
        extension.controller = mockController("private String s;", true, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        final Pattern pattern = Pattern.compile(JAVA.getCssClass());
        final Matcher matcher = pattern.matcher(content);

        assertTrue(matcher.find());
    }

    @Test
    public void textileLineSpecifierCodeMultiLineWithoutEmptyLines() {
        extension.controller = mockController("private String s;\nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + "). "));
    }

    @Test
    public void textileLineSpecifierCodeMultiLineWithEmptyLines() {
        extension.controller = mockController("private String s;\n\nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + ").. "));
    }

    @Test
    public void textileLineSpecifierCodeMultiLineWithLinesContainingOnlySpaces() {
        extension.controller = mockController("private String s;\n  \nprivate boolean b;", false, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + ").. "));
    }

    @Test
    public void textileLineSpecifierCodeSingleLineWithoutEmptyLines() {
        extension.controller = mockController("private String s;", false, JAVA);
        final String content = extension.buildContentString(textileMarkup);

        assertTrue(content.startsWith("bc(" + JAVA.getCssClass() + "). "));
    }

    @Test
    public void textileWithoutLanguageAndDontShowLines() {
        extension.controller = mockController("private String s;", false, null);
        final String content = extension.buildContentString(textileMarkup);

        assertTrue(content.startsWith("bc. "));
    }
}
