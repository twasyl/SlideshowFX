package com.twasyl.slideshowfx.content.extension.link;

import com.twasyl.slideshowfx.content.extension.link.controllers.LinkContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link LinkContentExtension} using a textile markup.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class TextileLinkContentExtensionTest extends BaseLinkContentExtensionTest {

    private static IMarkup markup;

    @BeforeAll
    public static void setUp() {
        markup = new TextileMarkup();
    }

    @Test
    public void withAddressAndTextAndNullMarkup() {
        mockController("http://slideshowfx.github.io", "SlideshowFX website");

        final String contentString = linkContentExtension.buildContentString(null);
        final String expected = "<a href=\"http://slideshowfx.github.io\">SlideshowFX website</a>";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndText() {
        mockController("http://slideshowfx.github.io", "SlideshowFX website");

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "\"SlideshowFX website\":http://slideshowfx.github.io";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndNullText() {
        mockController("http://slideshowfx.github.io", null);

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "\"http://slideshowfx.github.io\":http://slideshowfx.github.io";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndEmptyText() {
        mockController("http://slideshowfx.github.io", "");

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "\"http://slideshowfx.github.io\":http://slideshowfx.github.io";
        assertEquals(expected, contentString);
    }

    @Test
    public void withNullAddressAndText() {
        mockController(null, "SlideshowFW website");

        final String contentString = linkContentExtension.buildContentString(markup);
        assertEquals("", contentString);
    }

    @Test
    public void withEmptyAddressAndText() {
        mockController("", "SlideshowFW website");

        final String contentString = linkContentExtension.buildContentString(markup);
        assertEquals("", contentString);
    }
}