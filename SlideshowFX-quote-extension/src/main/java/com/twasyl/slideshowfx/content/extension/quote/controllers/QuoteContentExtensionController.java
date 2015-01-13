/*
 * Copyright 2015 Thierry Wasylczenko
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
