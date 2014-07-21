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

package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.ChoiceBoxTemplateElement;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;

/**
 * @author Thierry Wasylczenko
 */
public class ChoiceBoxDragableTemplateElement extends DragableTemplateElementLabel {

    private final ListProperty<String> values = new SimpleListProperty<>();

    public ChoiceBoxDragableTemplateElement() {
        super();
        this.setTemplateElementClassName(ChoiceBoxTemplateElement.class.getName());
    }

    public ListProperty<String> valuesProperty() { return this.values; }
    public ObservableList<String> getValues() { return this.values.get(); }
    public void setValues(ObservableList<String> values) { this.values.set(values); }
}
