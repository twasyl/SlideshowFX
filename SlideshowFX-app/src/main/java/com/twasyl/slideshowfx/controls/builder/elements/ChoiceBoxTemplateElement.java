/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;

/**
 * The ChoiceBoxTemplateElement allows to choose a value from a {@link javafx.scene.control.ChoiceBox}.
 * It implements {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}. The choices can be passed using
 * the {@link #addChoice(String...)} .
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ChoiceBoxTemplateElement extends AbstractTemplateElement<String> {

    private final ListProperty<String> choices = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ChoiceBoxTemplateElement(String name) {
        super();

        this.name.set(name);

        final ChoiceBox<String> choiceBox = new ChoiceBox<String>();
        choiceBox.itemsProperty().bindBidirectional(choices);
        this.value.bind(choiceBox.getSelectionModel().selectedItemProperty());

        this.appendContent(choiceBox);
    }

    /**
     * Add choices to the ChoiceBox contained in this element.
     * @param choices The choices to add to the ChoiceBox
     */
    public void addChoice(String ... choices) {
        if(choices != null && choices.length > 0) {
            this.choices.addAll(choices);
        }
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(String.format("\"%1$s\"", getValue()));

        return builder.toString();
    }
}
