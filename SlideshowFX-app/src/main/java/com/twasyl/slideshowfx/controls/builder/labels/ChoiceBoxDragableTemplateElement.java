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
