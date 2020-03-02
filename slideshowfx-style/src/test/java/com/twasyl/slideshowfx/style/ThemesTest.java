package com.twasyl.slideshowfx.style;

import com.twasyl.slideshowfx.style.theme.Theme;
import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@ExtendWith(ApplicationExtension.class)
@DisplayName("The themes")
public class ThemesTest {

    private static Collection<Theme> THEMES;
    private Parent parent;

    @Start
    void onStart(final Stage stage) {
        parent = new Button("Button");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    @BeforeAll
    public static void setUp() {
        THEMES = Themes.read();
    }

    @Test
    @DisplayName("must be of the number of CSS files")
    public void correctNumbersOfThemes() throws URISyntaxException {
        final File directory = new File(ThemesTest.class.getResource("/com/twasyl/slideshowfx/style/theme/css").toURI());
        final File[] cssFiles = directory.listFiles(pathname -> pathname.getName().endsWith(".css"));

        assertEquals(2, cssFiles.length);
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

    @Nested
    @DisplayName("should have a light theme")
    public class LightTest {

        @Test
        @DisplayName("named 'Light'")
        public void nameIsDark() {
            final Theme theme = THEMES.stream().filter(t -> "Light".equals(t.getName())).findAny().orElseThrow(IllegalArgumentException::new);
            assertEquals("Light", theme.getName());
        }

        @Test
        @DisplayName("with an existing 'light.css' file")
        public void hasDarkTheme() throws URISyntaxException {
            final Theme theme = THEMES.stream().filter(t -> "Light".equals(t.getName())).findAny().orElseThrow(IllegalArgumentException::new);
            assertTrue(new File(theme.getCssFile().toURI()).exists());
        }
    }

    @DisplayName("must exclude them mutually when applying a theme")
    @Test
    public void applyTheme() {
        Themes.applyTheme(parent, "Dark");
        Themes.applyTheme(parent, "Light");

        final ObservableList<String> stylesheets = parent.getStylesheets();
        assertNotNull(stylesheets);
        assertFalse(stylesheets.isEmpty());
        assertEquals(1, stylesheets.size());
        assertEquals(Themes.getByName("Light").getCssFile().toExternalForm(), stylesheets.get(0));
    }
}
