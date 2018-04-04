package com.twasyl.slideshowfx.content.extension.quote.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.ui.controls.ZoomTextArea;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * This class is the controller used by the {@code QuoteContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class QuoteContentExtensionController extends AbstractContentExtensionController {

    @FXML
    private ZoomTextArea quote;
    @FXML
    private TextField author;

    /**
     * Get the quote entered in the UI.
     *
     * @return The quote entered in the UI.
     */
    public String getQuote() {
        return this.quote.getText();
    }

    /**
     * Get the author of the quote inserted in the UI.
     *
     * @return The author inserted in the UI.
     */
    public String getAuthor() {
        return this.author.getText();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.quote.validProperty());

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.quote.setValidator(isNotEmpty());
    }
}
