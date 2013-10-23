package com.twasyl.lat.scene.controls.slide;

import com.twasyl.lat.scene.controls.layout.Layout;
import com.twasyl.lat.scene.controls.layout.LayoutElement;
import com.twasyl.lat.scene.controls.slide.SlideElement;
import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

@DefaultProperty(value = "elements")
public class Slide extends AnchorPane {

    private final ObjectProperty<Layout> layout = new SimpleObjectProperty<>();
    private final ListProperty<SlideElement> elements = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<SlideElement>()));

    public Slide() {

    }

    public ObjectProperty<Layout> layoutProperty() { return this.layout; }
    public Layout getLayout() { return this.layoutProperty().get(); }
    public void setLayout(Layout layout) { this.layoutProperty().set(layout); }

    public ListProperty<SlideElement> elementsProperty() { return this.elements; }
    public ObservableList<SlideElement> getElements() { return this.elementsProperty().get(); }
    public void setElements(ObservableList<SlideElement> elements) { this.elementsProperty().set(elements); }

    public ReadOnlyListProperty<Node> buildSlide() {
        ReadOnlyListProperty<Node> completeSlide = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<Node>()));

        for(SlideElement element : getElements()) {
            completeSlide.add((Node) element);
        }

        if(getLayout() != null) {
            for(LayoutElement child : getLayout().getElements()) {
                if(child.isStaticElement()) {
                    completeSlide.add((Node) child);
                }
            }
        }

        return completeSlide;
    }

    public String getSlideStyle() {
        if((getStyle() == null || getStyle().trim().isEmpty()) && getLayout() != null) return getLayout().getStyle();
        else return getStyle();
    }
}
