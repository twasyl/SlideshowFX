package com.twasyl.lat.scene.controls.slide;

import com.twasyl.lat.scene.controls.layout.LayoutLabel;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class SlideLabel extends Label implements SlideElement<LayoutLabel> {

    private final ObjectProperty<LayoutLabel> referenceLayoutElement = new SimpleObjectProperty<>();

    public SlideLabel() {
        referenceLayoutElementProperty().addListener(new ChangeListener<Label>() {
            @Override
            public void changed(ObservableValue<? extends Label> observableValue, Label label, Label label2) {
               if (label2 != null) {
                    setLayoutX(getReferenceLayoutElement().getLayoutX());
                    setLayoutY(getReferenceLayoutElement().getLayoutY());
                    setEffect(label2.getEffect());
                    setStyle(getReferenceLayoutElement().getStyle());
                    setAlignment(label2.getAlignment());
                    setTextAlignment(label2.getTextAlignment());
                } else {
                    layoutXProperty().unbind();
                    layoutYProperty().unbind();
                    styleProperty().unbind();
               }
            }
        });

        parentProperty().addListener(new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends Parent> observableValue, Parent parent, Parent parent2) {
                if(parent2 != null) {

                    ((Pane) parent2).prefWidthProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                            switch(getAlignment()) {
                                case BASELINE_CENTER:
                                    setLayoutX((number2.doubleValue() / 2) - (getWidth() / 2));
                                    break;
                            }
                        }
                    });
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
