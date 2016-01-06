/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.content.extension.link.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code QuoteContentExtension.fxml} file. The field containing the address
 * in the UI will be initialized by the content of the {@link Clipboard} if it contains a text having an URL form.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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

    /**
     * Get the URL stored in the {@link Clipboard clipboard}. This method uses{@link #doesClipboardContainsURL()} to
     * determine if the {@link Clipboard clipboard} contains an URL.
     * @return The URL stored in the {@link Clipboard clipboard} or {@code null} if none.
     */
    private String getClipboardURL() {
        final String url;

        if(doesClipboardContainsURL()) {
            if(Clipboard.getSystemClipboard().hasUrl()) {
                url = Clipboard.getSystemClipboard().getUrl();
            } else {
                url = Clipboard.getSystemClipboard().getString();
            }
        } else {
            url = null;
        }

        return url;
    }

    /**
     * Check if the {@link Clipboard} contains an URL. The check is performed by calling {@link Clipboard#hasUrl()} and
     * if it returns {@code false} then {@link Clipboard#hasString()} is called to check if the text looks like an URL.
     * @return {@code true} if the clipboard contains an URL, {@code false} otherwise.
     */
    private boolean doesClipboardContainsURL() {
        boolean containsUrl = Clipboard.getSystemClipboard().hasUrl();

        if(!containsUrl && Clipboard.getSystemClipboard().hasString()) {
            containsUrl = isTextAnURL(Clipboard.getSystemClipboard().getString());
        }

        return containsUrl;
    }

    /**
     * Check if a given text is an URL. The test is case un-sensitive and check is the text starts with one of the
     * following:
     * <ul>
     *     <li>http://</li>
     *     <li>https://</li>
     *     <li>www.</li>
     * </ul>
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
    public void initialize(URL location, ResourceBundle resources) {
        final String url = getClipboardURL();

        if(url != null) this.address.setText(url);
    }
}
