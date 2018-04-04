package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import com.twasyl.slideshowfx.markup.IMarkup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for testing the {@link CodeContentExtension} class.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public abstract class BaseCodeContentExtensionTest {

    protected static CodeContentExtension extension;
    protected static IMarkup markup;

    protected CodeContentExtensionController mockController(final String code, final boolean showLineNumbers, final SupportedLanguage language) {
        final CodeContentExtensionController controller = mock(CodeContentExtensionController.class);
        when(controller.getCode()).thenReturn(code);
        when(controller.isShowingLineNumbers()).thenReturn(showLineNumbers);
        when(controller.getLanguage()).thenReturn(language);

        return controller;
    }
}
