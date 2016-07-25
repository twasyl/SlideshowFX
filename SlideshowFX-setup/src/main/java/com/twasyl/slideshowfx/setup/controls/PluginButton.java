package com.twasyl.slideshowfx.setup.controls;

import com.twasyl.slideshowfx.setup.controllers.PluginsViewController;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Implementation of a {@link ToggleButton} representing a plugin to install or not. The button has a CSS class named
 * {@code plugin-button}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class PluginButton extends ToggleButton {
    private static final double BUTTON_SIZE = 80;

    public PluginButton(final PluginsViewController.Plugin plugin) {
        this.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        this.setTooltip(new Tooltip(plugin.label + ":\n" +plugin.description));
        this.getStyleClass().add("plugin-button");

        final VBox graphics = new VBox(2);
        graphics.setAlignment(Pos.CENTER);

        if(plugin.icon != null) {
            graphics.getChildren().add(plugin.icon);
        } else {
            final Text label = new Text(plugin.label);
            label.setWrappingWidth(75);
            label.setTextAlignment(TextAlignment.CENTER);
            graphics.getChildren().add(label);
        }

        this.setGraphic(graphics);
    }
}
