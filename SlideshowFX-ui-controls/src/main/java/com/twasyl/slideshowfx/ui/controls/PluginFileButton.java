package com.twasyl.slideshowfx.ui.controls;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a {@link ToggleButton} representing a file of a plugin. The button has a CSS class named
 * {@code plugin-file-button}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PluginFileButton extends ToggleButton {
    private static Logger LOGGER = Logger.getLogger(PluginFileButton.class.getName());

    private static final double BUTTON_SIZE = 80;

    private File pluginFile;

    public PluginFileButton(final File pluginFile) {
        this.pluginFile = pluginFile;
        final Attributes manifestAttributes = this.getManifestAttributes();

        final Node icon = this.buildIconNode(manifestAttributes);
        final String label = this.getManifestAttributeValue(manifestAttributes, "Setup-Wizard-Label", this.pluginFile.getName());
        final String version = this.getManifestAttributeValue(manifestAttributes, "Bundle-Version", "");
        final String description = this.getManifestAttributeValue(manifestAttributes, "Bundle-Description", "");

        this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);

        this.getStyleClass().add("plugin-file-button");

        final VBox graphics = new VBox(2);
        graphics.setAlignment(Pos.CENTER);

        if(icon != null) {
            graphics.getChildren().add(icon);
        } else {
            final Text labelElement = new Text(label);
            labelElement.getStyleClass().add("text");
            labelElement.setWrappingWidth(75);
            labelElement.setTextAlignment(TextAlignment.CENTER);
            graphics.getChildren().add(labelElement);
        }

        final Text versionElement = new Text(version);
        versionElement.getStyleClass().add("text");
        versionElement.setWrappingWidth(75);
        versionElement.setTextAlignment(TextAlignment.CENTER);
        graphics.getChildren().add(versionElement);

        this.setGraphic(graphics);

        this.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            final StringBuilder tooltipText = new StringBuilder(label).append(":\n")
                    .append(description).append(".\n");

            if(newSelected) tooltipText.append("Will be installed");
            else tooltipText.append("Will not be installed");

            tooltipText.append('.');

            Tooltip tooltip = this.getTooltip();
            if(tooltip == null) {
                tooltip = new Tooltip();
                this.setTooltip(tooltip);
            }

            tooltip.setText(tooltipText.toString());
        });
    }

    public File getFile() {
        return this.pluginFile;
    }

    /**
     * Get the attributes contained in the {@code MANIFEST.MF} of the plugin.
     * @return The attributes contained in the {@code MANIFEST.MF} file of the plugin.
     */
    protected final Attributes getManifestAttributes() {
        Attributes manifestAttributes = null;

        try {
            final JarFile jarFile = new JarFile(this.pluginFile);
            final Manifest manifest = jarFile.getManifest();
            manifestAttributes = manifest.getMainAttributes();
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Can not extract manifest attributes", ex);
        }

        return manifestAttributes;
    }

    /**
     * Get the value of an attribute stored within a collection of {@code attributes}. If the value is {@code null} or
     * empty, the default value will be returned.
     * @param attributes The whole collection of attributes.
     * @param name The name of the attribute to retrieve the value for.
     * @param defaultValue The default value to return if the original value is {@code null} or empty.
     * @return The value of the attribute.
     */
    protected final String getManifestAttributeValue(final Attributes attributes, final String name, final String defaultValue) {
        final String value = attributes.getValue(name);

        if(value == null || value.isEmpty()) return defaultValue;
        else return value;
    }

    /**
     * Get the icon of the plugin stored within the JAR file as an array of bytes. If no icon is present, an empty array
     * is returned.
     * @param plugin The JAR file of the plugin.
     * @return The icon of the plugin.
     */
    protected final byte[] getIconFromJar(final File plugin) {
        final ByteArrayOutputStream iconOut = new ByteArrayOutputStream();

        try {
            final JarFile jarFile = new JarFile(plugin);
            final JarEntry icon = jarFile.getJarEntry("META-INF/icon.png");

            if(icon != null) {
                final InputStream iconIn = jarFile.getInputStream(icon);
                final byte[] buffer = new byte[512];
                int numberOfBytesRead;

                while ((numberOfBytesRead = iconIn.read(buffer)) != -1) {
                    iconOut.write(buffer, 0, numberOfBytesRead);
                }

                iconOut.flush();
                iconOut.close();
            }
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Can not the icon from JAR", ex);
        }

        return iconOut.toByteArray();
    }

    /**
     * Create the {@code Node} that will contain the icon of the plugin.
     * @param attributes The manifest attributes of the plugin JAR file.
     * @return The element containing the icon of the plugin.
     */
    protected final Node buildIconNode(final Attributes attributes) {
        Node icon = null;
        final byte[] iconFromJar = this.getIconFromJar(this.pluginFile);

        if(iconFromJar != null && iconFromJar.length > 0) {
            final ByteArrayInputStream input = new ByteArrayInputStream(iconFromJar);
            final Image image = new Image(input, 50, 50, true, true);
            icon = new ImageView(image);
        } else {
            final String fontIconName = this.getManifestAttributeValue(attributes, "Setup-Wizard-Icon-Name", "");

            if(!fontIconName.isEmpty()) {
                icon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(fontIconName));
                ((FontAwesomeIconView) icon).setGlyphSize(50);
            }
        }

        return icon;
    }
}
