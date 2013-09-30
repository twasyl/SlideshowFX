package com.twasyl.lat.scene.controls;

import com.twasyl.lat.app.LookAtThis;
import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.stage.Screen;

import java.util.ArrayList;

@DefaultProperty("slides")
public class Presentation extends Group {

    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty author = new SimpleStringProperty();
    private final ListProperty<Slide> slides = new SimpleListProperty<>();
    private final ListProperty<Layout> layouts = new SimpleListProperty<>();
    private final ReadOnlyObjectProperty<Slide> currentSlide = new SimpleObjectProperty<>();

    private final BooleanProperty started = new SimpleBooleanProperty(false);
    private final IntegerProperty currentSlideIndex = new SimpleIntegerProperty(-1);

    public Presentation() {
        this.currentSlideIndex.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                Slide slide = null;
                if (number2 != null) {
                    slide = Presentation.this.getSlides().get(number2.intValue());
                }

                ((SimpleObjectProperty<Slide>) Presentation.this.currentSlide).set(slide);
                Presentation.this.showSlide();
            }
        });
    }

    public StringProperty titleProperty() { return this.title; }
    public String getTitle() { return this.titleProperty().get(); }
    public void setTitle(String title) { this.titleProperty().set(title); }

    public StringProperty authorProperty() { return this.author; }
    public String getAuthor() { return this.authorProperty().get(); }
    public void setAuthor(String author) { this.authorProperty().set(author); }

    public ListProperty<Slide> slidesProperty() { return this.slides; }
    public ObservableList<Slide> getSlides() { return this.slidesProperty().get(); }
    public void setSlides(ObservableList<Slide> slides) { this.slides.set(slides); }

    public ListProperty<Layout> layoutsProperty() { return this.layouts; }
    public ObservableList<Layout> getLayouts() { return this.layoutsProperty().get(); }
    public void setLayouts(ObservableList<Layout> layouts) { this.layoutsProperty().set(layouts); }

    public ReadOnlyObjectProperty<Slide> currentSlideProperty() { return this.currentSlide; }
    public Slide getCurrentSlide() { return this.currentSlideProperty().get(); }

    public void start() {
        if(!getSlides().isEmpty()) {
            this.currentSlideIndex.set(0);
            started.set(true);
            LookAtThis.getStage().setFullScreen(true);
        } else {
            started.set(false);
        }
    }

    public void next() {
        if(started.get()) {
            int index = currentSlideIndex.get() + 1;

            if(index < getSlides().size()) {
                currentSlideIndex.set(index);
            }
        }
    }

    public void previous() {
        if(started.get()) {
            int index = currentSlideIndex.get() - 1;

            if(index >= 0) {
                currentSlideIndex.set(index);
            }
        }
    }

    private void showSlide() {
        getChildren().clear();

        if (getCurrentSlide() != null) {
            getChildren().add(getCurrentSlide());

            double displayWidth, displayHeight;
            if(LookAtThis.getStage().isFullScreen()) {
                final Rectangle2D screnSize = Screen.getPrimary().getBounds();
                displayWidth = screnSize.getWidth();
                displayHeight = screnSize.getHeight();
            } else {
                displayWidth = LookAtThis.getStage().getWidth();
                displayHeight = LookAtThis.getStage().getHeight();
            }

            double slideWidth = getCurrentSlide().getPrefWidth();
            double slideHeight = getCurrentSlide().getPrefHeight();


            double widthRatio = displayWidth / slideWidth;
            double heightRatio = displayHeight / slideHeight;

            double scaleX;
            double scaleY;

            if (widthRatio == heightRatio) {
                scaleX = widthRatio;
                scaleY = heightRatio;
            } else {
                scaleX = heightRatio;
                scaleY = heightRatio;
            }

            double centerX = (displayWidth / 2) - (slideWidth / 2);
            double centerY = (displayHeight / 2) - (slideHeight / 2);

            setTranslateX(centerX);
            setTranslateY(centerY);

            setScaleX(scaleX);
            setScaleY(scaleY);
        }
    }
}
