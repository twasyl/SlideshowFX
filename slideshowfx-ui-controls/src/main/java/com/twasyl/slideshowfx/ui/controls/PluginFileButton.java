package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import com.twasyl.slideshowfx.plugin.manager.internal.RegisteredPlugin;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

import static javafx.geometry.Pos.TOP_RIGHT;

/**
 * Implementation of a {@link ToggleButton} representing a file of a plugin. The button has a CSS class named
 * {@code plugin-file-button}.
 *
 * @author Thierry Wasylczenko
 * @version 1.4-SNAPSHOT
 * @since SlideshowFX 1.1
 */
public class PluginFileButton extends ToggleButton {
    private static Logger LOGGER = Logger.getLogger(PluginFileButton.class.getName());

    private static final double BUTTON_SIZE = 80;

    private RegisteredPlugin plugin;
    private final String label;
    private final String version;
    private final String description;
    private boolean hasBadge = false;
    private String badgeDescription;

    public PluginFileButton(final PluginFile pluginFile) {
        this.plugin = new RegisteredPlugin(pluginFile);

        final Node icon = this.buildIconNode();
        this.label = this.plugin.getName();
        this.version = this.plugin.getVersion();
        this.description = this.plugin.getDescription();

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

        this.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> this.setTooltipText());
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

        if (hasBadge) {
            text.append("\n").append(badgeDescription);
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
    public PluginFile getFile() {
        return this.plugin.getFile();
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
     * Append the given {@link Icon} as badge on the top right corner of the button. The badge will have a CSS class
     * named {@code badge}.
     *
     * @param badge The icon to set as badge.
     */
    public void appendBadge(final Icon badge, final String badgeDescription) {
        if (!hasBadge) {
            final VBox graphic = (VBox) getGraphic();
            final Node currentIcon = graphic.getChildren().get(0);

            final FontAwesome icon = new FontAwesome(badge);
            icon.getStyleClass().add("badge");

            final StackPane stack = new StackPane();
            stack.setAlignment(TOP_RIGHT);
            stack.getChildren().addAll(currentIcon, icon);

            graphic.getChildren().set(0, stack);

            this.hasBadge = true;
            this.badgeDescription = badgeDescription;

            setTooltipText();
        } else {
            LOGGER.info("A badge has already been set");
        }
    }

    /**
     * Create the {@code Node} that will contain the icon of the plugin.
     *
     * @return The element containing the icon of the plugin.
     */
    protected final Node buildIconNode() {
        Node icon = null;
        final byte[] iconFromJar = this.plugin.getIcon();

        if (iconFromJar != null && iconFromJar.length > 0) {
            final ByteArrayInputStream input = new ByteArrayInputStream(iconFromJar);
            final Image image = new Image(input, 50, 50, true, true);
            icon = new ImageView(image);
        } else {
            final String fontIconName = this.plugin.getIconName();

            if (!fontIconName.isEmpty()) {
                icon = new FontAwesome(Icon.valueOf(fontIconName));
            }
        }

        if (icon != null) {
            icon.getStyleClass().add("icon");
        }

        return icon;
    }
}
