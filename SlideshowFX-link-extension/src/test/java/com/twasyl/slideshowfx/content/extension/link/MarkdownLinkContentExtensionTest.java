package com.twasyl.slideshowfx.content.extension.link;

import com.twasyl.slideshowfx.content.extension.link.controllers.LinkContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link LinkContentExtension} using a markdown markup.
 * @author Thierry Wasylczenko
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class MarkdownLinkContentExtensionTest {

    private static IMarkup markup;
    private static LinkContentExtension linkContentExtension;

    @BeforeClass
    public static void setUp() {
        markup = new MarkdownMarkup();
        linkContentExtension = new LinkContentExtension();
        linkContentExtension.controller =  mock(LinkContentExtensionController.class);
    }

    @Test
    public void withAddressAndTextAndNullMarkup() {
        when(linkContentExtension.controller.getAddress()).thenReturn("http://slideshowfx.github.io");
        when(linkContentExtension.controller.getText()).thenReturn("SlideshowFX website");

        final String contentString = linkContentExtension.buildContentString(null);
        final String expected = "<a href=\"http://slideshowfx.github.io\">SlideshowFX website</a>";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndText() {
        when(linkContentExtension.controller.getAddress()).thenReturn("http://slideshowfx.github.io");
        when(linkContentExtension.controller.getText()).thenReturn("SlideshowFX website");

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "[SlideshowFX website](http://slideshowfx.github.io)";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndNullText() {
        when(linkContentExtension.controller.getAddress()).thenReturn("http://slideshowfx.github.io");
        when(linkContentExtension.controller.getText()).thenReturn(null);

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "http://slideshowfx.github.io";
        assertEquals(expected, contentString);
    }

    @Test
    public void withAddressAndEmptyText() {
        when(linkContentExtension.controller.getAddress()).thenReturn("http://slideshowfx.github.io");
        when(linkContentExtension.controller.getText()).thenReturn("");

        final String contentString = linkContentExtension.buildContentString(markup);
        final String expected = "http://slideshowfx.github.io";
        assertEquals(expected, contentString);
    }

    @Test
    public void withNullAddressAndText() {
        when(linkContentExtension.controller.getAddress()).thenReturn(null);
        when(linkContentExtension.controller.getText()).thenReturn("SlideshowFW website");

        final String contentString = linkContentExtension.buildContentString(markup);
        assertEquals("", contentString);
    }

    @Test
    public void withEmptyAddressAndText() {
        when(linkContentExtension.controller.getAddress()).thenReturn("");
        when(linkContentExtension.controller.getText()).thenReturn("SlideshowFW website");

        final String contentString = linkContentExtension.buildContentString(markup);
        assertEquals("", contentString);
    }
}