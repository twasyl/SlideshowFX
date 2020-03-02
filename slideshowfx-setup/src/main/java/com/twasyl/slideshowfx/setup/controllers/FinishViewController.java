package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.setup.app.SetupProperties;
import com.twasyl.slideshowfx.utils.OSUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the {FinishView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class FinishViewController implements Initializable {

    @FXML
    private VBox root;
    @FXML
    private Text applicationName;
    @FXML
    private Text applicationVersion;
    private CheckBox createDesktopShortcut;
    private CheckBox startAfterInstallation;

    /**
     * Set the name of the application this setup is for.
     *
     * @param applicationName The name of the application.
     * @return This instance of the controller.
     */
    public FinishViewController setApplicationName(final String applicationName) {
        this.applicationName.setText(applicationName);
        return this;
    }

    /**
     * Set the version of the application this setup is for.
     *
     * @param applicationVersion The version of the application.
     * @return This instance of the controller.
     */
    public FinishViewController setApplicationVersion(final String applicationVersion) {
        this.applicationVersion.setText(applicationVersion);
        return this;
    }

    /**
     * Indicates if the user has chosen to create a desktop shortcut.
     *
     * @return {@code true} if a desktop shortcut should be created, {@code false} otherwise.
     */
    public boolean createDesktopShortcut() {
        return this.createDesktopShortcut != null && this.createDesktopShortcut.isSelected();
    }

    /**
     * Indicates if the user has chosen to start the application after the installation.
     *
     * @return {@code true} if the application should be started after the installation process, {@code false} otherwise.
     */
    public boolean startApplicationAfterInstallation() {
        return this.startAfterInstallation != null && this.startAfterInstallation.isSelected();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (OSUtils.isWindows()) {
            this.createDesktopShortcut = new CheckBox("Create desktop shortcut");
            this.root.getChildren().addAll(this.createDesktopShortcut);
        }

        if (OSUtils.isMac() || OSUtils.isWindows()) {
            this.startAfterInstallation = new CheckBox("Start " + SetupProperties.getInstance().getApplicationName() + " after the installation");
            this.root.getChildren().addAll(this.startAfterInstallation);
        }
    }
}
