package com.twasyl.slideshowfx.controls.slideshow;

import com.twasyl.slideshowfx.controls.PresentationBrowser;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalTime;

/**
 * A pane that is used to display information about the presentation when the presentation mode is active. It displays
 * the current slide of the presentation, the next one and the time elapsed since the beginning of the presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class InformationPane extends StackPane {
    private final Context context;

    private final Timeline timeline = new Timeline();
    private final Text timeElapsed = new Text();
    private final PresentationBrowser currentSlideBrowser = new PresentationBrowser();
    private final PresentationBrowser nextSlideBrowser = new PresentationBrowser();

    private LocalTime beginningTime;

    public InformationPane(final Context context) {
        super();

        this.setAlignment(Pos.TOP_LEFT);
        this.getStylesheets().addAll(
                ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/Default.css"),
                ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/information-scene.css"));

        this.getStyleClass().add("information-scene");

        this.context = context;

        this.initializeTimeElapsed();
        this.initializeCurrentSlide();
        this.initializeNextSlide();
    }

    /**
     * Initialize the node that displays the time elapsed since the presentation has started.
     */
    private final void initializeTimeElapsed() {
        this.timeElapsed.getStyleClass().add("time-elapsed");

        final DoubleProperty fontSize = new SimpleDoubleProperty(0);
        this.timeElapsed.fontProperty().addListener((fontValue, oldFont, newFont) -> {
            if (newFont != null) {
                fontSize.set(newFont.getSize());
            }
        });

        this.timeElapsed.setTranslateX(50);
        this.timeElapsed.translateYProperty().bind(this.heightProperty().subtract(fontSize).subtract(50));
        this.getChildren().add(this.timeElapsed);
    }

    /**
     * Initialize the node displaying the current slide of the presentation.
     */
    private final void initializeCurrentSlide() {
        final DoubleBinding width = this.widthProperty().divide(1.8);
        final DoubleBinding height = this.heightProperty().divide(1.8);

        this.currentSlideBrowser.prefWidthProperty().bind(width);
        this.currentSlideBrowser.minWidthProperty().bind(width);
        this.currentSlideBrowser.maxWidthProperty().bind(width);

        this.currentSlideBrowser.prefHeightProperty().bind(height);
        this.currentSlideBrowser.minHeightProperty().bind(height);
        this.currentSlideBrowser.maxHeightProperty().bind(height);

        this.currentSlideBrowser.setTranslateX(50);
        this.currentSlideBrowser.setTranslateY(50);

        this.currentSlideBrowser.setInteractionAllowed(false);
        this.currentSlideBrowser.setBackend(this);
        this.getChildren().add(this.currentSlideBrowser);
    }

    /**
     * Initialize the node displaying the next slide of the presentation.
     */
    private final void initializeNextSlide() {
        final DoubleBinding width = this.widthProperty().divide(3.5);
        final DoubleBinding height = this.heightProperty().divide(3.5);

        this.nextSlideBrowser.prefWidthProperty().bind(width);
        this.nextSlideBrowser.minWidthProperty().bind(width);
        this.nextSlideBrowser.maxWidthProperty().bind(width);

        this.nextSlideBrowser.prefHeightProperty().bind(height);
        this.nextSlideBrowser.minHeightProperty().bind(height);
        this.nextSlideBrowser.maxHeightProperty().bind(height);

        this.nextSlideBrowser.translateXProperty().bind(this.widthProperty().subtract(width).subtract(50));
        this.nextSlideBrowser.setTranslateY(50);

        this.nextSlideBrowser.setInteractionAllowed(false);
        this.nextSlideBrowser.setBackend(this);

        this.getChildren().add(this.nextSlideBrowser);
    }

    /**
     * Start the information scene. This starts the monitoring of the time elapsed since the beginning of the presentation,
     * initialize the current and next slides.
     */
    public void start() {
        this.beginningTime = LocalTime.now();

        this.timeline.getKeyFrames().add(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
            final Duration duration = Duration.between(this.beginningTime, LocalTime.now());
            final long numberOfHours = duration.getSeconds() / 3600;
            final long numberOfMinutes = (duration.getSeconds() % 3600) / 60;
            final long numberOfSeconds = (duration.getSeconds() % 3600) % 60;

            this.timeElapsed.setText(
                    String.format("%1$02d:%02$02d:%3$02d", numberOfHours, numberOfMinutes, numberOfSeconds)
            );
        }));

        this.timeline.setCycleCount(Animation.INDEFINITE);
        this.timeline.playFromStart();
    }

    /**
     * Stops the information scene.
     */
    public void stop() {
        this.timeline.stop();
    }

    public void sendKey(KeyEvent event) {
        this.currentSlideBrowser.fireEvent(event);
        this.nextSlideBrowser.fireEvent(event);
    }

    /**
     * Get the browser that displays the next slide of the presentation.
     * @return The browser displaying the next slide of the presentation.
     */
    public PresentationBrowser getNextSlideBrowser() { return nextSlideBrowser; }

    /**
     * Get the browser that displays the current slide of the presentation.
     * @return The browser displaying the current slide of the presentation.
     */
    public PresentationBrowser getCurrentSlideBrowser() { return currentSlideBrowser; }

    /**
     * Move the presentation to the next slide. The nodes displaying the current and next slides are updated accordingly.
     */
    public void nextSlide() {
        this.currentSlideBrowser.nextSlide();

        final String currentSlideId = this.currentSlideBrowser.getCurrentSlideId();
        if(currentSlideId != null) {
            final Slide currentDisplayedSlide = this.context.getPresentation().getConfiguration().getSlideById(currentSlideId);
            final int slideIndex = this.context.getPresentation().getConfiguration().getSlides().indexOf(currentDisplayedSlide);

            if(slideIndex != -1 && slideIndex < this.context.getPresentation().getConfiguration().getSlides().size() - 1) {
                this.nextSlideBrowser.slide(this.context.getPresentation().getConfiguration().getSlides().get(slideIndex + 1).getId());
            }
        }
    }

    /**
     * Move the presentation to the previous slide. The nodes displaying the current and next slides are updated accordingly.
     */
    public void previousSlide() {
        this.currentSlideBrowser.previousSlide();

        final String currentSlideId = this.currentSlideBrowser.getCurrentSlideId();
        if(currentSlideId != null) {
            final Slide currentDisplayedSlide = this.context.getPresentation().getConfiguration().getSlideById(currentSlideId);
            final int slideIndex = this.context.getPresentation().getConfiguration().getSlides().indexOf(currentDisplayedSlide);

            if(slideIndex != -1 && slideIndex > 0) {
                this.nextSlideBrowser.slide(this.context.getPresentation().getConfiguration().getSlides().get(slideIndex + 1).getId());
            }
        }
    }
}
