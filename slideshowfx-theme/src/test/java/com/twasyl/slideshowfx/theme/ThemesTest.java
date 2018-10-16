package com.twasyl.slideshowfx.theme;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@DisplayName("The themes")
public class ThemesTest {

    private static Collection<Theme> THEMES;

    @BeforeAll
    public static void setUp() {
        THEMES = Themes.read();
    }

    @Test
    @DisplayName("must be of the number of CSS files")
    public void correctNumbersOfThemes() throws URISyntaxException {
        final File directory = new File(ThemesTest.class.getResource("/com/twasyl/slideshowfx/theme/css").toURI());
        final File[] cssFiles = directory.listFiles(pathname -> pathname.getName().endsWith(".css"));

        assertEquals(1, cssFiles.length);
    }

    @Nested
    @DisplayName("should have a dark theme")
    public class DarkTest {

        @Test
        @DisplayName("named 'Dark'")
        public void nameIsDark() {
            final Theme theme = THEMES.stream().filter(t -> "Dark".equals(t.getName())).findAny().orElseThrow(IllegalArgumentException::new);
            assertEquals("Dark", theme.getName());
        }

        @Test
        @DisplayName("with an existing 'dark.css' file")
        public void hasDarkTheme() throws URISyntaxException {
            final Theme theme = THEMES.stream().filter(t -> "Dark".equals(t.getName())).findAny().orElseThrow(IllegalArgumentException::new);
            assertTrue(new File(theme.getCssFile().toURI()).exists());
        }
    }
}
