/*
 * Copyright 2014 Thierry Wasylczenko
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
 * This class is the controller used by the {@code QuoteContentExtension.fxml} file.
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(Clipboard.getSystemClipboard().hasUrl()) {
            final String url = Clipboard.getSystemClipboard().getUrl();
            if (url != null) this.address.setText(url);
        }
    }
}
