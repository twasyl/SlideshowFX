package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

public class SlideMenuItem extends MenuItem {

    private final ObjectProperty<Slide> slide = new SimpleObjectProperty<>();

    public SlideMenuItem(Slide slide) {
        if(slide == null) throw new IllegalArgumentException("The slide can not be null");

        this.slide.set(slide);

        Node graphic;

        if(slide.getThumbnail() != null) {
            ImageView view = new ImageView(slide.getThumbnail());
            view.setFitWidth(200);
            view.setSmooth(true);
            view.setPreserveRatio(true);

            graphic = view;
        } else {
            Label text = new Label(String.format("%1$s #%2$s", slide.getTemplate().getName(), slide.getSlideNumber()));

            graphic = text;
        }

       setGraphic(graphic);
    }

    public ObjectProperty<Slide> slideProperty() { return slide; }
    public Slide getSlide() { return slideProperty().get(); }
    public void setSlide(Slide slide) { this.slideProperty().set(slide); }
}
