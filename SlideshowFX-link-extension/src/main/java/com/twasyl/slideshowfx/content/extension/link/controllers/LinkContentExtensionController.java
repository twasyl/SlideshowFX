package com.twasyl.slideshowfx.content.extension.link.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code QuoteContentExtension.fxml} file.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class LinkContentExtensionController implements Initializable {

    @FXML private TextField address;
    @FXML private TextField text;

    /**
     * Get the address of the link entered in the UI.
     * @return The address of the link entered in the UI.
     */
    public String getAddress() { return this.address.getText(); }

    /**
     * Get the text of the link inserted in the UI.
     * @return The text of the link inserted in the UI.
     */
    public String getText() { return this.text.getText(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
