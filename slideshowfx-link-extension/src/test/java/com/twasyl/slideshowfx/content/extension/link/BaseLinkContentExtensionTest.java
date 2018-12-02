package com.twasyl.slideshowfx.content.extension.link;

import com.twasyl.slideshowfx.content.extension.link.controllers.LinkContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.*;

/**
 * Base class for testing the {@link LinkContentExtension} class.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@TestInstance(PER_CLASS)
public abstract class BaseLinkContentExtensionTest {

    protected static LinkContentExtension linkContentExtension;
    protected static IMarkup markup;

    @BeforeAll
    static void setup() {
        linkContentExtension = spy(LinkContentExtension.class);
        final LinkContentExtensionController controller = mock(LinkContentExtensionController.class);
        doReturn(controller).when(linkContentExtension).getController();
    }

    protected void mockController(final String address, final String text) {
        final LinkContentExtensionController controller = linkContentExtension.getController();
        doReturn(address).when(controller).getAddress();
        doReturn(text).when(controller).getText();
    }
}
