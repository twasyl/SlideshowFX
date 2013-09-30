package com.twasyl.lat.scene.controls.slide;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public interface SlideElement {

    final StringProperty referenceLayoutElement = new SimpleStringProperty();

    public StringProperty referenceLayoutElementProperty();
    public String getReferenceLayoutElement();
    public void setReferenceLayoutElement(String referenceLayoutElement);
}
