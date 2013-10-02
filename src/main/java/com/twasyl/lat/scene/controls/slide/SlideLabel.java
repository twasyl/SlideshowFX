package com.twasyl.lat.scene.controls.slide;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class SlideLabel extends Label implements SlideElement<Label> {

    private final ObjectProperty<Label> referenceLayoutElement = new SimpleObjectProperty<>();

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
    public ObjectProperty<Label> referenceLayoutElementProperty() {
        return referenceLayoutElement;
    }

    @Override
    public Label getReferenceLayoutElement() {
        return referenceLayoutElementProperty().get();
    }

    @Override
    public void setReferenceLayoutElement(Label referenceLayoutElement) {
        referenceLayoutElementProperty().set(referenceLayoutElement);
    }
}
