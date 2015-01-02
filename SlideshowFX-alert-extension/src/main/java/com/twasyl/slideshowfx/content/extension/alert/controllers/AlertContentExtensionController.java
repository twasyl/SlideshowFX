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

package com.twasyl.slideshowfx.content.extension.alert.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code AlertContentExtension.fxml} file.
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class AlertContentExtensionController implements Initializable {

    @FXML private TextField title;
    @FXML private TextField text;
    @FXML private ToggleGroup typeGroup;
    @FXML private RadioButton infoRB;
    @FXML private RadioButton successRB;
    @FXML private RadioButton warningRB;
    @FXML private RadioButton errorRB;
    @FXML private CheckBox allowClickOutside;
    @FXML private CheckBox showCancelButton;
    @FXML private TextField buttonText;

    /**
     * Return the title entered in the UI for this alert.
     * @return The title of the alert.
     */
    public String getTitle() { return this.title.getText(); }

    /**
     * Return the additional text entered in the UI for this alert.
     * @return The text for this alert.
     */
    public String getText() { return this.text.getText(); }

    /**
     * Return the type of the alert. Currently four values can be returned:
     * <ul>
     *     <li>info</li>
     *     <li>success</li>
     *     <li>warning</li>
     *     <li>error</li>
     * </ul>
     *
     * @return Return the type of this alert.
     */
    public String getType() {
        String type = null;

        if(this.typeGroup.getSelectedToggle() == this.infoRB) type = "info";
        else if(this.typeGroup.getSelectedToggle() == this.successRB) type = "success";
        else if(this.typeGroup.getSelectedToggle() == this.warningRB) type = "warning";
        else if(this.typeGroup.getSelectedToggle() == this.errorRB) type = "error";

        return type;
    }

    public boolean isClickOutsideAllowed() { return this.allowClickOutside.isSelected(); }

    public boolean isCancelButtonVisible() { return this.showCancelButton.isSelected(); }

    public String getButtonText() { return this.buttonText.getText(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
