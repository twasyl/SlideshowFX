package com.twasyl.slideshowfx.icons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static com.twasyl.slideshowfx.icons.FontType.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.stage.Stage;

/**
 * Test of class {@link FontAwesome}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
@ExtendWith(ApplicationExtension.class)
public class FontAwesomeTest {

    @Start
    void onStart(final Stage stage) {
        stage.show();
    }

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
    public void obtainJavaScriptFontFile() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeJSFile());
    }

    @Test
    public void obtainJavaScriptFileFromRelativePath() throws URISyntaxException {
        assertFontAwesomeUrl(FontAwesome.getFontAwesomeFile("js/all.min.js"));
    }
}
