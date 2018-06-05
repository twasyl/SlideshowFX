package com.twasyl.slideshowfx.icons;

import javafx.beans.property.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Class defining FontAwesome icons.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class FontAwesome extends Text {
    private static class FontCacheKey {
        private double size;
        private FontType type;

        public FontCacheKey(double size, FontType type) {
            this.size = size;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontCacheKey that = (FontCacheKey) o;
            return Double.compare(that.size, size) == 0 &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(size, type);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(FontAwesome.class.getName());
    private static final String FONTAWESOME_VERSION = "5.0.13";
    private static final String FONTAWESOME_ROOT = "/com/twasyl/slideshowfx/icons/fontawesome/" + FONTAWESOME_VERSION.replaceAll("\\.", "_") + "/";
    private static final Map<FontCacheKey, Font> FONT_CACHE = new HashMap<>();

    private final ObjectProperty<Icon> icon = new SimpleObjectProperty<>(Icon.FOLDER_OPEN);
    private final DoubleProperty size = new SimpleDoubleProperty(10d);
    private final StringProperty color = new SimpleStringProperty("white");

    public FontAwesome() {
        setText(getIcon().getUnicode());
        this.setFont(getFontAwesomeFont(getIcon(), 10d));
        this.definePropertyListeners();
        recomputeStyle();
    }

    public FontAwesome(final Icon icon) {
        this();
        setIcon(icon);
    }

    public FontAwesome(final Icon icon, final Double size) {
        this(icon);
        this.setSize(size);
    }

    protected Font getFontAwesomeFont(final Icon icon, final double size) {
        final Font font;

        final FontCacheKey key = new FontCacheKey(size, icon.getType());
        synchronized (FONT_CACHE) {
            if (FONT_CACHE.containsKey(key)) {
                LOGGER.fine("Returned cached font for size " + size);
                font = FONT_CACHE.get(key);
            } else {
                LOGGER.fine("Font not found in cache for size " + size);
                try (final InputStream stream = FontAwesome.getFontAwesomeFontFile(icon.getType()).openStream()) {
                    font = Font.loadFont(stream, size);
                    FONT_CACHE.put(key, font);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return font;
    }

    protected void definePropertyListeners() {
        this.icon.addListener((value, oldIcon, newIcon) -> {
            this.setFont(getFontAwesomeFont(newIcon, getSize()));
            setText(newIcon.getUnicode());
            recomputeStyle();
        });

        this.size.addListener((value, oldSize, newSize) -> {
            this.setFont(getFontAwesomeFont(getIcon(), newSize.doubleValue()));
            recomputeStyle();
        });

        this.color.addListener((value, oldSize, newSize) -> recomputeStyle());
    }

    public ObjectProperty<Icon> iconProperty() {
        return icon;
    }

    public Icon getIcon() {
        return icon.get();
    }

    public void setIcon(Icon icon) {
        this.icon.set(icon);
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public Double getSize() {
        return size.get();
    }

    public void setSize(Double size) {
        this.size.set(size);
    }

    public StringProperty colorProperty() {
        return color;
    }

    public String getColor() {
        return color.get();
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    protected void recomputeStyle() {
        setStyle(String.format("-fx-fill: %s;", getColor()));
    }

    /**
     * Get the FontAwesome version provided by SlideshowFX.
     *
     * @return The version of FontAwesome.
     */
    public static String getFontAwesomeVersion() {
        return FONTAWESOME_VERSION;
    }

    /**
     * Get the font file for the desired type.
     *
     * @param type The type of the desired font.
     * @return The {@link URL} of the font file.
     */
    public static URL getFontAwesomeFontFile(final FontType type) {
        final StringBuilder path = new StringBuilder("fonts/fontawesome-")
                .append(type.name().toLowerCase()).append(".otf");

        return getFontAwesomeFile(path.toString());
    }

    /**
     * Get the CSS file of FontAwesome.
     *
     * @return The {@link InputStream} of the CSS font file.
     */
    public static URL getFontAwesomeCSSFile() {
        return getFontAwesomeFile("css/" + getFontAwesomeCSSFilename());
    }

    /**
     * Get the name of the FontAwesome CSS file. The returned named doesn't contain any path.
     *
     * @return The name of the FontAwesome CSS file.
     */
    public static String getFontAwesomeCSSFilename() {
        return "fa-svg-with-js.css";
    }

    /**
     * Get the JavaScript file of FontAwesome.
     *
     * @return The {@link URL} of the JavaScript font file.
     */
    public static URL getFontAwesomeJSFile() {
        return getFontAwesomeFile("js/" + getFontAwesomeJSFilename());
    }

    /**
     * Get the name of the FontAwesome JavaScript file. The returned named doesn't contain any path.
     *
     * @return The name of the FontAwesome JavaScript file.
     */
    public static String getFontAwesomeJSFilename() {
        return "fontawesome-all.min.js";
    }

    /**
     * <p>Get a FontAwesome file from a given relative path. The path is relative from the <i>root</i> package
     * where all FontAwesome files are stored within the JAR.</p>
     *
     * @param relativePath The relative path of the file to get
     * @return The {@link URL} of the FontAwesome file.
     */
    public static URL getFontAwesomeFile(final String relativePath) {
        return FontAwesome.class.getResource(FONTAWESOME_ROOT + relativePath);
    }
}
