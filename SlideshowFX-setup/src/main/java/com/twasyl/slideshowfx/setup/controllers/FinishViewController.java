package com.twasyl.slideshowfx.setup.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the {FinishView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class FinishViewController implements Initializable {

    @FXML private Text applicationName;
    @FXML private Text applicationVersion;

    /**
     * Set the name of the application this setup is for.
     * @param applicationName The name of the application.
     * @return This instance of the controller.
     */
    public FinishViewController setApplicationName(final String applicationName) {
        this.applicationName.setText(applicationName);
        return this;
    }

    /**
     * Set the version of the application this setup is for.
     * @param applicationVersion The version of the application.
     * @return This instance of the controller.
     */
    public FinishViewController setApplicationVersion(final String applicationVersion) {
        this.applicationVersion.setText(applicationVersion);
        return this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
