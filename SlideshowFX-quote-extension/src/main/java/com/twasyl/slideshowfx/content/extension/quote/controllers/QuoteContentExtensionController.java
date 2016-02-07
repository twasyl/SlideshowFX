package com.twasyl.slideshowfx.content.extension.quote.controllers;

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
 * @since SlideshowFX 1.0.0
 */
public class QuoteContentExtensionController implements Initializable {

    @FXML private TextArea quote;
    @FXML private TextField author;

    /**
     * Get the quote entered in the UI.
     * @return The quote entered in the UI.
     */
    public String getQuote() { return this.quote.getText(); }

    /**
     * Get the author of the quote inserted in the UI.
     * @return The author inserted in the UI.
     */
    public String getAuthor() { return this.author.getText(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
