package com.twasyl.lat.scene.controls.slide;

import javafx.beans.property.ObjectProperty;

public interface  SlideElement<T> {
    ObjectProperty<T> referenceLayoutElementProperty();
    T getReferenceLayoutElement();
    void setReferenceLayoutElement(T referenceLayoutElement);
}
