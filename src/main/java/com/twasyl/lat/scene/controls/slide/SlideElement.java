package com.twasyl.lat.scene.controls.slide;

import com.twasyl.lat.scene.controls.layout.LayoutElement;
import javafx.beans.property.ObjectProperty;

public interface  SlideElement<T extends LayoutElement> {
    ObjectProperty<T> referenceLayoutElementProperty();
    T getReferenceLayoutElement();
    void setReferenceLayoutElement(T referenceLayoutElement);
}
