package com.twasyl.lat.scene.controls.slide;

import com.twasyl.lat.scene.controls.layout.LayoutLabel;
import com.twasyl.lat.scene.controls.layout.LayoutVBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class SlideVBox extends VBox implements SlideElement<LayoutVBox> {

    private final ObjectProperty<LayoutVBox> referenceLayoutElement = new SimpleObjectProperty<>();

    public SlideVBox() {
        referenceLayoutElementProperty().addListener(new ChangeListener<VBox>() {
            @Override
            public void changed(ObservableValue<? extends VBox> observableValue, VBox vbox, VBox vbox2) {
                if (vbox2 != null) {
                    setLayoutX(getReferenceLayoutElement().getLayoutX());
                    setLayoutY(getReferenceLayoutElement().getLayoutY());
                    setEffect(vbox2.getEffect());
                    setStyle(getReferenceLayoutElement().getStyle());
                    setAlignment(vbox2.getAlignment());
                }
            }
        });
    }

    @Override
    public ObjectProperty<LayoutVBox> referenceLayoutElementProperty() {
        return referenceLayoutElement;
    }

    @Override
    public LayoutVBox getReferenceLayoutElement() {
        return referenceLayoutElementProperty().get();
    }

    @Override
    public void setReferenceLayoutElement(LayoutVBox referenceLayoutElement) {
        referenceLayoutElementProperty().set(referenceLayoutElement);
    }
}
