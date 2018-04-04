package com.twasyl.slideshowfx.icons;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.twasyl.slideshowfx.icons.FontType.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of class {@link FontAwesome}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class FontAwesomeTest {

    protected void assertFontAwesomeUrl(final URL url) throws URISyntaxException {
        assertNotNull(url);
        final File file = new File(url.toURI());
        assertTrue(file.exists(), "URL doesn't exist: " + url.toExternalForm());
    }

    @Test
    public void obtainRegularFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFontFile(REGULAR));
    }

    @Test
    public void obtainSolidFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFontFile(SOLID));
    }

    @Test
    public void obtainBrandFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFontFile(BRAND));
    }

    @Test
    public void obtainCSSFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeCSSFile());
    }

    @Test
    public void obtainJavaScriptFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeJSFile());
    }

    @Test
    public void obtainJavaScriptFileFromRelativePath() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFile("js/fontawesome-all.min.js"));
    }

    @Test
    public void obtainCSSFileFromRelativePath() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFile("css/fa-svg-with-js.css"));
    }
}
