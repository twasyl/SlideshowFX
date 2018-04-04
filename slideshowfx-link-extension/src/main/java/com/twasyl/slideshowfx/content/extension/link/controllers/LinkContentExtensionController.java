package com.twasyl.slideshowfx.content.extension.link.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;

import java.net.URL;
import java.util.ResourceBundle;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * This class is the controller used by the {@code QuoteContentExtension.fxml} file. The field containing the address
 * in the UI will be initialized by the content of the {@link Clipboard} if it contains a text having an URL form.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class LinkContentExtensionController extends AbstractContentExtensionController {

    @FXML
    private ExtendedTextField address;
    @FXML
    private ExtendedTextField text;

    /**
     * Get the address of the link entered in the UI.
     *
     * @return The address of the link entered in the UI.
     */
    public String getAddress() {
        return this.address.getText();
    }

    /**
     * Get the text of the link inserted in the UI.
     *
     * @return The text of the link inserted in the UI.
     */
    public String getText() {
        return this.text.getText();
    }

    /**
     * Get the URL that is present in the system clipboard. This method ensures that if the
     * {@link Clipboard#hasUrl()} returns {@code true}, then URL is truly an URL.
     * If the clipboard doesn't contain an URL according both {@link Clipboard#hasUrl()} and
     * {@link Clipboard#getUrl()}, then the text is checked in order to determine if there is a true
     * URL as text in the clipboard.
     *
     * @return The URL or {@code null} if none.
     */
    private String getClipboardURL() {
        boolean hasUrl = Clipboard.getSystemClipboard().hasUrl();
        String url = null;

        if (hasUrl) {
            if (isTextAnURL(Clipboard.getSystemClipboard().getUrl())) {
                url = Clipboard.getSystemClipboard().getUrl();
            } else {
                hasUrl = false;
            }
        }

        if (!hasUrl && Clipboard.getSystemClipboard().hasString()) {
            if (isTextAnURL(Clipboard.getSystemClipboard().getString())) {
                url = Clipboard.getSystemClipboard().getString();
            }
        }

        return url;
    }

    /**
     * Check if a given text is an URL. The test is case un-sensitive and check is the text starts with one of the
     * following:
     * <ul>
     * <li>http://</li>
     * <li>https://</li>
     * <li>www.</li>
     * </ul>
     *
     * @param text The text to test.
     * @return {@code true} if the text has an URL form, {@code false} otherwise, including {@code null}.
     */
    private boolean isTextAnURL(final String text) {
        final String lowerCasedText = text != null ? text.toLowerCase() : null;
        boolean isURL = lowerCasedText != null && (
                lowerCasedText.startsWith("http://") ||
                        lowerCasedText.startsWith("https://") ||
                        lowerCasedText.startsWith("www.")
        );
        return isURL;
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.address.validProperty());

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.address.setValidator(isNotEmpty());
        final String url = getClipboardURL();

        if (url != null) this.address.setText(url);
    }
}
