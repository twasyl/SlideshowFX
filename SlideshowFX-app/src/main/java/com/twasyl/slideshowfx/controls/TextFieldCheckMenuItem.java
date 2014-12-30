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

package com.twasyl.slideshowfx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * A menu item containing a checkbox and a textfield for entering a value. In order to set the value, the user has to
 * press {@code ENTER} in the textfield. The value can then be retrieved using {@link #valueProperty()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class TextFieldCheckMenuItem extends CustomMenuItem {

    private final StringProperty value = new SimpleStringProperty();

    private TextField textField;
    private CheckBox checkBox;

    public TextFieldCheckMenuItem() {
        this.setHideOnClick(false);

        this.initBehavior();
        this.initUI();
    }

    private final void initBehavior() {
        this.checkBox = new CheckBox() {
            @Override
            public void requestFocus() {
            }
        };

        this.checkBox.textProperty().bind(this.textProperty());

        this.textField = new TextField() {
            @Override
            public void requestFocus() {
                super.requestFocus();
                TextFieldCheckMenuItem.this.setSelected(true);
            }
        };

        // Set the value for this component when the user validate the field, usually pressing ENTER
        this.textField.setOnAction(event -> this.setValue(this.textField.getText()));

        this.valueProperty().addListener((value, oldValue, newValue) -> this.textField.setText(newValue));
    }

    private final void initUI() {
        this.textField.setPrefColumnCount(5);

        final HBox content = new HBox(5, this.checkBox, this.textField);
        content.setAlignment(Pos.BASELINE_LEFT);
        this.setContent(content);
    }

    /**
     * Get the value that is inserted in the text field of this component.
     * @return The property indicating the value of this menu item.
     */
    public StringProperty valueProperty() { return this.value; }

    /**
     * Get the value that is inserted in the text field of this component.
     * @return The value for this component.
     */
    public String getValue() { return valueProperty().get(); }

    /**
     * Set the value if this menu item. When the value is set, the textfield of this menu item will also contain the
     * value.
     * @param value The new value for this menu item.
     */
    public void setValue(String value) { this.valueProperty().set(value); }

    /**
     * Indicates if the menu item is selected.
     * @return The property indicating if the menu item is selected.
     */
    public BooleanProperty selectedProperty() { return this.checkBox.selectedProperty(); }

    /**
     * Indicates if the menu item is selected.
     * @return {@code true} if the menu item is selected, {@code false} otherwise.
     */
    public boolean isSelected() { return this.selectedProperty().get(); }

    /**
     * Select or not this menu item.
     * @param selected The new state of this menu item.
     */
    public void setSelected(boolean selected) { this.selectedProperty().set(selected); }
}
