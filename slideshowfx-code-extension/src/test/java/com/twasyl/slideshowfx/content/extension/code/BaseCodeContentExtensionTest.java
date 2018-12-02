package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import com.twasyl.slideshowfx.markup.IMarkup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.*;

/**
 * Base class for testing the {@link CodeContentExtension} class.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
@TestInstance(PER_CLASS)
public abstract class BaseCodeContentExtensionTest {

    protected static CodeContentExtension extension;
    protected static IMarkup markup;

    @BeforeAll
    static void setup() {
        extension = spy(CodeContentExtension.class);
    }

    protected CodeContentExtensionController mockController(final String code, final boolean showLineNumbers, final SupportedLanguage language, String highlightedLines) {
        final CodeContentExtensionController controller = mock(CodeContentExtensionController.class);
        when(controller.getCode()).thenReturn(code);
        when(controller.isShowingLineNumbers()).thenReturn(showLineNumbers);
        when(controller.getLanguage()).thenReturn(language);

        if (highlightedLines != null) {
            when(controller.shouldHighlightLines()).thenReturn(true);
            when(controller.getHightlightedLines()).thenReturn(highlightedLines);
        }

        return controller;
    }

}
