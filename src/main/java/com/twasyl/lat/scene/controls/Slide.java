package com.twasyl.lat.scene.controls;

import com.twasyl.lat.scene.controls.slide.SlideElement;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

@DefaultProperty("elements")
public class Slide extends AnchorPane {

    private final ObjectProperty<Layout> layout = new SimpleObjectProperty<>();
    private final ListProperty<SlideElement> elements = new SimpleListProperty<>();

    public ObjectProperty<Layout> layoutProperty() { return this.layout; }
    public Layout getLayout() { return this.layoutProperty().get(); }
    public void setLayout(Layout layout) { this.layoutProperty().set(layout); }

    public ListProperty<SlideElement> elementsProperty() { return this.elements; }
    public ObservableList<SlideElement> getElements() { return this.elementsProperty().get(); }
    public void setElements(ObservableList<SlideElement> elements) { this.elementsProperty().set(elements); }
}
