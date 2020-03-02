package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.setup.app.SetupProperties;
import com.twasyl.slideshowfx.utils.OSUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


/**
 * Controller for the {InstallationLocationView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class InstallationLocationViewController implements Initializable {

    @FXML
    private TextField location;

    /**
     * Get the chosen destination by the user.
     *
     * @return The chosen destination.
     */
    public String getLocation() {
        return this.location.getText();
    }

    @FXML
    private void chooseDestination(final ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Installation location");

        final File directory = chooser.showDialog(location.getScene().getWindow());

        if (directory != null && directory.canWrite()) {
            this.location.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    private void dragOverLocation(final DragEvent event) {
        if (event.getDragboard().hasFiles() && event.getDragboard().getFiles().size() == 1) {
            event.acceptTransferModes(TransferMode.LINK);
            event.consume();
        }
    }

    @FXML
    private void dragDropOnLocation(final DragEvent event) {
        boolean dragSuccess = false;

        if (event.getDragboard().hasFiles() && event.isAccepted()) {
            final File droppedFile = event.getDragboard().getFiles().get(0);
            final File location = droppedFile.isFile() ? droppedFile.getParentFile() : droppedFile;

            this.location.setText(location.getAbsolutePath());
        }

        event.setDropCompleted(dragSuccess);
        event.consume();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String defaultLocation;

        if (SetupProperties.getInstance().getDefaultInstallationLocation() != null) {
            defaultLocation = SetupProperties.getInstance().getDefaultInstallationLocation().getAbsolutePath();
        } else if (OSUtils.isWindows()) {
            defaultLocation = System.getenv("LOCALAPPDATA");
        } else if (OSUtils.isMac()) {
            defaultLocation = "/Applications";
        } else {
            defaultLocation = System.getProperty("user.home");
        }

        this.location.setText(defaultLocation);
    }
}
