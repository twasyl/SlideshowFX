package com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * This class is the controller used by the {@code SequenceDiagramContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SequenceDiagramContentExtensionController extends AbstractContentExtensionController {
    private static final Logger LOGGER = Logger.getLogger(SequenceDiagramContentExtensionController.class.getName());

    @FXML private Hyperlink jumlyLink;
    @FXML private ZoomTextArea sequence;

    public String getSequenceDiagramText() {
        return this.sequence.getText();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(sequence.validProperty());

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.jumlyLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI("http://jumly.tmtk.net/reference.html"));
            } catch (IOException | URISyntaxException e) {
                LOGGER.log(Level.SEVERE, "Can not open the documentation", e);
            }
        });

        this.sequence.setValidator(isNotEmpty());
    }
}
