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

package com.twasyl.slideshowfx.content.extension.code.controllers;

import com.twasyl.slideshowfx.content.extension.code.enums.SupportedLanguage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code CodeContentExtension.fxml} file.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class CodeContentExtensionController implements Initializable {

    @FXML private ChoiceBox<SupportedLanguage> language;
    @FXML private TextArea code;
    @FXML private ColorPicker backgroundColor;

    /**
     * Get the selected programming language in the UI.
     * @return The selected programming language in the UI.
     */
    public SupportedLanguage getLanguage() { return this.language.getValue();  }

    /**
     * Get the code that has been entered in the UI.
     * @return The code entered in the UI.
     */
    public String getCode() { return this.code.getText(); }

    /**
     * Get the background color for the code that will be displayed in the presentation.
     * @return The background color.
     */
    public Color getBackgroundColor() { return this.backgroundColor.getValue(); }

    /**
     * Get the background color for the code that will be displayed in the presentation as hexedecimal string. For the
     * the lightgray color, the string returned will be {@code #D3D3D3}.
     * @return The background color as hexadecimal string.
     */
    public String getBackgroundColorHexadecimal() {
        return "#" + Integer.toHexString(getBackgroundColor().hashCode()).substring(0, 6).toUpperCase();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.backgroundColor.setValue(Color.LIGHTGRAY);

        this.language.setConverter(new StringConverter<SupportedLanguage>() {
            @Override
            public String toString(SupportedLanguage object) { return object.getName(); }

            @Override
            public SupportedLanguage fromString(String string) {
                return SupportedLanguage.fromName(string);
            }
        });

        this.language.getItems().addAll(SupportedLanguage.values());
    }
}
