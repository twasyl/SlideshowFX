package com.twasyl.lat.scene.controls.slide;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

public class SlideLabel extends Label implements SlideElement {

    @Override
    public StringProperty referenceLayoutElementProperty() {
        return referenceLayoutElement;
    }

    @Override
    public String getReferenceLayoutElement() {
        return referenceLayoutElementProperty().get();
    }

    @Override
    public void setReferenceLayoutElement(String referenceLayoutElement) {
        referenceLayoutElementProperty().set(referenceLayoutElement);
    }
}
