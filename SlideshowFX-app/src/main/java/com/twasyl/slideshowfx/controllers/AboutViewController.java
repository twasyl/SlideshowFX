package com.twasyl.slideshowfx.controllers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class of the {@code AboutView.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class AboutViewController implements Initializable {
    private Logger LOGGER = Logger.getLogger(AboutViewController.class.getName());

    @FXML private Parent root;
    @FXML private Label slideshowFXVersion;
    @FXML private Label javaVersion;

    @FXML
    public void exitByClick(final MouseEvent event) {
        this.closeStage();
    }

    protected void closeStage() {
        final Event closeEvent = new WindowEvent(this.root.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST);
        this.root.getScene().getWindow().fireEvent(closeEvent);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.slideshowFXVersion.setText(String.format("SlideshowFX version: %1$s", getApplicationVersion()));
        this.javaVersion.setText(String.format("Java version: %1$s", System.getProperty("java.version")));
    }

    /**
     * Get the version of the application. The version is stored within the {@code MANIFEST.MF} file of the {@link JarFile}
     * of the application.
     * @return The version of the application stored in the {@code MANIFEST.MF} file or {@code null} if it can not be found.
     */
    private String getApplicationVersion() {
        String appVersion = null;

        try(final JarFile jarFile = new JarFile(getJARLocation())) {
            final Manifest manifest = jarFile.getManifest();
            final Attributes attrs = manifest.getMainAttributes();
            if(attrs != null) {
                appVersion = attrs.getValue("Implementation-Version");
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not get application's version", e);
        }

        return appVersion;
    }

    /**
     * Get the {@link File} that corresponds to the JAR file of the application.
     * @return The file corresponding to JRA file of the application.
     * @throws URISyntaxException If the location can not be determined.
     */
    private File getJARLocation() throws URISyntaxException {
        final File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        return file;
    }
}
