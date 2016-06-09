package com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller used by the {@code SequenceDiagramContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class SequenceDiagramContentExtensionController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(SequenceDiagramContentExtensionController.class.getName());

    @FXML private Hyperlink jumlyLink;
    @FXML private TextArea sequence;

    public String getSequenceDiagramText() {
        return this.sequence.getText();
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
    }
}
