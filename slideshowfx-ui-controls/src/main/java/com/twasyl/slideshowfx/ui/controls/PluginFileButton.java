package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.utils.Jar;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a {@link ToggleButton} representing a file of a plugin. The button has a CSS class named
 * {@code plugin-file-button}.
 *
 * @author Thierry Wasylczenko
 * @version 1.3
 * @since SlideshowFX 1.1
 */
public class PluginFileButton extends ToggleButton {
    private static Logger LOGGER = Logger.getLogger(PluginFileButton.class.getName());

    private static final double BUTTON_SIZE = 80;

    private Jar pluginFile;
    private final String label;
    private final String version;
    private final String description;

    public PluginFileButton(final File pluginFile) {
        try {
            this.pluginFile = new Jar(pluginFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JAR file", e);
        }

        final Node icon = this.buildIconNode();
        this.label = this.pluginFile.getManifestAttributeValue("Setup-Wizard-Label", this.pluginFile.getFile().getName());
        this.version = this.pluginFile.getManifestAttributeValue("Bundle-Version", "");
        this.description = this.pluginFile.getManifestAttributeValue("Bundle-Description", "");

        this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);

        this.getStyleClass().add("plugin-file-button");

        final VBox graphics = new VBox(2);
        graphics.setAlignment(Pos.CENTER);

        if (icon != null) {
            graphics.getChildren().add(icon);
        } else {
            graphics.getChildren().add(getLabelNode());
        }

        graphics.getChildren().add(getVersionNode());

        this.setGraphic(graphics);
        this.setTooltipText();

        this.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
            this.setTooltipText();
        });

        try {
            this.pluginFile.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not close plugin file", e);
        }
    }

    protected void setTooltipText() {
        Tooltip tooltip = this.getTooltip();
        if (tooltip == null) {
            tooltip = new Tooltip();
            this.setTooltip(tooltip);
        }

        final StringBuilder text = new StringBuilder(label).append(":\n")
                .append(description).append(".\n");

        if (this.isSelected()) {
            text.append("Will be installed");
        } else {
            text.append("Will not be installed");
        }

        tooltip.setText(text.toString());
    }

    protected Text getVersionNode() {
        final Text versionElement = new Text(this.version);
        versionElement.getStyleClass().add("text");
        versionElement.setWrappingWidth(75);
        versionElement.setTextAlignment(TextAlignment.CENTER);

        return versionElement;
    }

    protected Text getLabelNode() {
        final Text labelElement = new Text(this.label);
        labelElement.getStyleClass().add("text");
        labelElement.setWrappingWidth(75);
        labelElement.setTextAlignment(TextAlignment.CENTER);

        return labelElement;
    }

    /**
     * Get the file associated to this button.
     *
     * @return The file associated to this button.
     */
    public File getFile() {
        return this.pluginFile.getFile();
    }

    /**
     * Get the label of this plugin.
     *
     * @return The label of this plugin.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Get the icon of the plugin stored within the JAR file as an array of bytes. If no icon is present, an empty array
     * is returned.
     *
     * @return The icon of the plugin.
     */
    protected final byte[] getIconFromJar() {
        final ByteArrayOutputStream iconOut = new ByteArrayOutputStream();

        final JarEntry icon = this.pluginFile.getEntry("META-INF/icon.png");

        if (icon != null) {
            try (final InputStream iconIn = this.pluginFile.getInputStream(icon)) {
                final byte[] buffer = new byte[512];
                int numberOfBytesRead;

                while ((numberOfBytesRead = iconIn.read(buffer)) != -1) {
                    iconOut.write(buffer, 0, numberOfBytesRead);
                }

                iconOut.flush();
                iconOut.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not the icon from JAR", e);
            }
        }

        return iconOut.toByteArray();
    }

    /**
     * Create the {@code Node} that will contain the icon of the plugin.
     *
     * @return The element containing the icon of the plugin.
     */
    protected final Node buildIconNode() {
        Node icon = null;
        final byte[] iconFromJar = this.getIconFromJar();

        if (iconFromJar != null && iconFromJar.length > 0) {
            final ByteArrayInputStream input = new ByteArrayInputStream(iconFromJar);
            final Image image = new Image(input, 50, 50, true, true);
            icon = new ImageView(image);
        } else {
            final String fontIconName = this.pluginFile.getManifestAttributeValue("Setup-Wizard-Icon-Name", "");

            if (!fontIconName.isEmpty()) {
                icon = new FontAwesome(Icon.valueOf(fontIconName), 50d);
                ((FontAwesome) icon).setIconColor(Color.BLACK);
            }
        }

        if (icon != null) {
            icon.getStyleClass().add("icon");
        }

        return icon;
    }
}
