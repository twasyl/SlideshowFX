package com.twasyl.lat.scene.controls.layout;

import javafx.beans.property.BooleanProperty;

public interface LayoutElement<T> {

    BooleanProperty staticElementProperty();
    boolean isStaticElement();
    void setStaticElement(boolean value);
}
