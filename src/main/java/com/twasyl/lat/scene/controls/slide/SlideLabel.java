package com.twasyl.lat.scene.controls.slide;

import com.twasyl.lat.scene.controls.layout.LayoutLabel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class SlideLabel extends Label implements SlideElement<LayoutLabel> {

    private final ObjectProperty<LayoutLabel> referenceLayoutElement = new SimpleObjectProperty<>();

    public SlideLabel() {
        referenceLayoutElementProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observableValue, Label label, Label label2) {
               if (label2 != null) {
                    setLayoutX(getReferenceLayoutElement().getLayoutX());
                    setLayoutY(getReferenceLayoutElement().getLayoutY());
                    styleProperty().bind(getReferenceLayoutElement().styleProperty());
                } else {
                    layoutXProperty().unbind();
                    layoutYProperty().unbind();
                    styleProperty().unbind();
               }
            }
        });
    }

    @Override
    public ObjectProperty<LayoutLabel> referenceLayoutElementProperty() {
        return referenceLayoutElement;
    }

    @Override
    public LayoutLabel getReferenceLayoutElement() {
        return referenceLayoutElementProperty().get();
    }

    @Override
    public void setReferenceLayoutElement(LayoutLabel referenceLayoutElement) {
        referenceLayoutElementProperty().set(referenceLayoutElement);
    }
}
