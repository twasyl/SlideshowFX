package com.twasyl.slideshowfx.icons;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.css.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;

/**
 * Class defining FontAwesome icons.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
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
    private static final StyleablePropertyFactory<FontAwesome> FACTORY = new StyleablePropertyFactory<>(Text.getClassCssMetaData());
    private static final String FONTAWESOME_VERSION = "5.15.3";
    private static final String FONTAWESOME_ROOT = "/com/twasyl/slideshowfx/icons/fontawesome/" + FONTAWESOME_VERSION.replaceAll("\\.", "_") + "/";
    private static final Map<FontCacheKey, Font> FONT_CACHE = new HashMap<>();

    private final ObjectProperty<Icon> icon = new SimpleObjectProperty<>(Icon.FOLDER_OPEN);

    private final StyleableProperty<Paint> iconColor =
            FACTORY.createStyleablePaintProperty(this, "iconColor", "-fx-icon-color", s -> s.iconColor, Color.WHITE);
    private final StyleableDoubleProperty iconSize = new SimpleStyleableDoubleProperty(
            FACTORY.createSizeCssMetaData("-fx-icon-size", s -> s.iconSize, 10d), this, "iconSize");

    public FontAwesome() {
        getStyleClass().add("font-awesome");
        setText(getIcon().getUnicode());
        this.setFont(getFontAwesomeFont(getIcon(), iconSize.getValue().doubleValue()));
        this.definePropertyListeners();
        this.defineBindings();
    }

    public FontAwesome(final Icon icon) {
        this();
        setIcon(icon);
    }

    public FontAwesome(final Icon icon, final Double iconSize) {
        this(icon);
        this.iconSize.setValue(iconSize);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    protected Font getFontAwesomeFont(final Icon icon, final double size) {
        final Font font;

        final FontCacheKey key = new FontCacheKey(size, icon.getType());
        synchronized (FONT_CACHE) {
            if (FONT_CACHE.containsKey(key)) {
                LOGGER.log(FINE, "Returned cached font for size {0}", size);
                font = FONT_CACHE.get(key);
            } else {
                LOGGER.log(FINE, "Font not found in cache for size {0}", size);
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
            this.setFont(getFontAwesomeFont(newIcon, getIconSize()));
            setText(newIcon.getUnicode());
        });

        this.iconSize.addListener((value, oldSize, newSize) -> this.setFont(getFontAwesomeFont(getIcon(), newSize.doubleValue())));
    }

    protected void defineBindings() {
        fillProperty().bind((ObservableValue<? extends Paint>) iconColor);
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

    public DoubleProperty iconSizeProperty() {
        return iconSize;
    }

    public Double getIconSize() {
        return iconSize.get();
    }

    public void setIconSize(Double iconSize) {
        this.iconSize.set(iconSize);
    }

    public ObjectProperty<Paint> iconColorProperty() {
        return (ObjectProperty<Paint>) iconColor;
    }

    public Paint getIconColor() {
        return iconColor.getValue();
    }

    public void setIconColor(Paint color) {
        this.iconColor.setValue(color);
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
        return "all.min.js";
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
