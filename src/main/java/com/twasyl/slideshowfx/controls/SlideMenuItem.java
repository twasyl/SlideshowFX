package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.utils.PresentationBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

public class SlideMenuItem extends MenuItem {

    private final ObjectProperty<PresentationBuilder.Slide> slide = new SimpleObjectProperty<>();

    public SlideMenuItem(PresentationBuilder.Slide slide) {
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
            Label text = new Label(String.format("%1$s #%2$s", slide.getName(), slide.getSlideNumber()));

            graphic = text;
        }

       setGraphic(graphic);
    }

    public ObjectProperty<PresentationBuilder.Slide> slideProperty() { return slide; }
    public PresentationBuilder.Slide getSlide() { return slideProperty().get(); }
    public void setSlide(PresentationBuilder.Slide slide) { this.slideProperty().set(slide); }
}
