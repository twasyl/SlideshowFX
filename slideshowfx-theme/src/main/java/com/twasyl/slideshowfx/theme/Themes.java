package com.twasyl.slideshowfx.theme;

/*
 * Class working with {@link Themes} by providing reading and getting {@link Theme themes}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import javafx.scene.Parent;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class Themes {
    private static final Logger LOGGER = Logger.getLogger(Themes.class.getName());
    private static Collection<Theme> themes = new ArrayList<>();

    /**
     * Read the content of the themes definition and load all themes.
     *
     * @return The collection of read themes.
     */
    public static Collection<Theme> read() {
        synchronized (themes) {
            if (themes.isEmpty()) {
                try {
                    final InputSource document = new InputSource(Themes.class.getResourceAsStream("/com/twasyl/slideshowfx/theme/themes.xml"));
                    final XPath xPath = XPathFactory.newInstance().newXPath();
                    final NodeList nodes = (NodeList) xPath.evaluate("/themes/theme", document, XPathConstants.NODESET);

                    if (nodes.getLength() > 0) {
                        for (int index = 0; index < nodes.getLength(); index++) {
                            final Node item = nodes.item(index);
                            final Theme theme = new Theme();
                            theme.setName(item.getAttributes().getNamedItem("name").getTextContent());
                            theme.setCssFile(Themes.class.getResource(item.getAttributes().getNamedItem("cssFile").getTextContent()));
                            themes.add(theme);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(WARNING, "Error when reading the themes configuration", e);
                }
            }
        }

        return themes;
    }

    /**
     * Find a {@link Theme theme} by it's name.
     *
     * @param name The name of the theme tp find.
     * @return The {@link Theme} with the given name.
     * @throws ThemeNotFoundException If the theme wasn't found
     */
    public static Theme getByName(final String name) throws ThemeNotFoundException {
        return read().stream()
                .filter(theme -> theme.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new ThemeNotFoundException(name));
    }

    /**
     * Apply the given theme identified by it's name to the given parent.
     *
     * @param parent    The parent to apply the theme on.
     * @param themeName The name of the theme to apply.
     */
    public static void applyTheme(final Parent parent, final String themeName) {
        if (parent != null && themeName != null) {
            try {
                final Theme theme = getByName(themeName);
                parent.getStylesheets().add(theme.getCssFile().toExternalForm());
            } catch (ThemeNotFoundException e) {
                LOGGER.log(WARNING, "Can not apply theme", e);
            }
        }
    }
}
