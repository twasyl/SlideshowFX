package com.twasyl.lat.scene.controls.layout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.ImageView;

public class LayoutImageView extends ImageView implements LayoutElement<ImageView> {
    private final BooleanProperty staticElement = new SimpleBooleanProperty();

    @Override
    public BooleanProperty staticElementProperty() {
        return this.staticElement;
    }

    @Override
    public boolean isStaticElement() {
        return this.staticElementProperty().get();
    }

    @Override
    public void setStaticElement(boolean value) {
        this.staticElementProperty().set(value);
    }
}
