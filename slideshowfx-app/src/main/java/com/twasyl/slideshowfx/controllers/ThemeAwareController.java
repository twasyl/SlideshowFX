package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.global.configuration.GlobalConfigurationObserver;
import com.twasyl.slideshowfx.style.Styles;
import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public interface ThemeAwareController extends Initializable, GlobalConfigurationObserver {
    Logger LOGGER = Logger.getLogger(ThemeAwareController.class.getName());

    /**
     * Get the root {@link Parent} of the view.
     *
     * @return The root {@link Parent} of this view.
     */
    Parent getRoot();

    void postInitialize(URL location, ResourceBundle resources);

    default void applyApplicationStyle() {
        Styles.applyApplicationStyle(getRoot());
    }

    default void applyTheme(final String theme) {
        Themes.applyTheme(getRoot(), theme);
    }

    @Override
    default void updateHttpProxyHost(boolean forHttps, String oldHost, String newHost) {
        // Not concerned
    }

    @Override
    default void updateHttpProxyPort(boolean forHttps, Integer oldPort, Integer newPort) {
        // Not concerned
    }

    @Override
    default void initialize(URL location, ResourceBundle resources) {
        this.applyApplicationStyle();
        this.applyTheme(GlobalConfiguration.getThemeName());
        GlobalConfiguration.addObserver(this);
        this.postInitialize(location, resources);
    }

    @Override
    default void updateTheme(String oldTheme, String newTheme) {
        applyTheme(newTheme);
    }
}
