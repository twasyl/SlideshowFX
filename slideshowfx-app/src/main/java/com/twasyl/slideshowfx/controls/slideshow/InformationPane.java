package com.twasyl.slideshowfx.controls.slideshow;

import com.twasyl.slideshowfx.controls.PresentationBrowser;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.Duration;
import java.time.LocalTime;

/**
 * A pane that is used to display information about the presentation when the presentation mode is active. It displays
 * the current slide of the presentation, the next one and the time elapsed since the beginning of the presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class InformationPane extends StackPane {
    private final Context context;

    private final Timeline timeline = new Timeline();
    private final Text currentTime = new Text();
    private final Text timeElapsed = new Text();
    private final PresentationBrowser currentSlideBrowser = new PresentationBrowser();
    private final PresentationBrowser nextSlideBrowser = new PresentationBrowser();
    private final ScrollPane speakerNotesScrollPane = new ScrollPane();
    private final Text speakerNotes = new Text();

    private LocalTime beginningTime;

    public InformationPane(final Context context) {
        super();

        this.setAlignment(Pos.TOP_LEFT);
        this.getStylesheets().add(InformationPane.class.getResource("/com/twasyl/slideshowfx/css/application.css").toExternalForm());
        this.getStyleClass().add("information-scene");

        this.context = context;

        this.initializeCurrentTime();
        this.initializeTimeElapsed();
        this.initializeSpeakerNotes();
        this.initializeCurrentSlide();
        this.initializeNextSlide();
    }

    /**
     * Initialize the node that displays the time elapsed since the presentation has started.
     */
    private final void initializeCurrentTime() {
        this.currentTime.getStyleClass().add("current-time");
        this.currentTime.setTextAlignment(TextAlignment.RIGHT);

        this.currentTime.boundsInLocalProperty().addListener((bounds, oldBounds, newBounds) -> {
            this.currentTime.setTranslateX(this.getWidth() - newBounds.getWidth() - 50);
            this.currentTime.setTranslateY(this.timeElapsed.getTranslateY() - newBounds.getHeight() - 30);
        });

        this.getChildren().add(this.currentTime);
    }

    /**
     * Initialize the node that displays the time elapsed since the presentation has started.
     */
    private final void initializeTimeElapsed() {
        this.timeElapsed.getStyleClass().add("time-elapsed");
        this.timeElapsed.setTextAlignment(TextAlignment.RIGHT);

        this.timeElapsed.boundsInLocalProperty().addListener((bounds, oldBounds, newBounds) -> {
            final int margin = 50;
            this.timeElapsed.setTranslateX(this.getWidth() - newBounds.getWidth() - margin);
            this.timeElapsed.setTranslateY(this.getHeight() - newBounds.getHeight() - margin);
        });

        this.getChildren().add(this.timeElapsed);
    }

    /**
     * Initialize the display of the speaker notes.
     */
    private void initializeSpeakerNotes() {
        final DoubleBinding width = this.widthProperty().divide(1.8);
        final DoubleBinding scrollPaneWidth = width.add(10);
        final DoubleBinding height = this.heightProperty().divide(3);

        this.speakerNotes.getStyleClass().addAll("text", "speaker-notes");
        this.speakerNotes.wrappingWidthProperty().bind(width);
        this.speakerNotes.setTextAlignment(TextAlignment.JUSTIFY);

        this.speakerNotesScrollPane.getStyleClass().add("speaker-notes-pane");
        this.speakerNotesScrollPane.setContent(this.speakerNotes);
        this.speakerNotesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.speakerNotesScrollPane.prefWidthProperty().bind(scrollPaneWidth);
        this.speakerNotesScrollPane.minWidthProperty().bind(scrollPaneWidth);
        this.speakerNotesScrollPane.maxWidthProperty().bind(scrollPaneWidth);

        this.speakerNotesScrollPane.prefHeightProperty().bind(height);
        this.speakerNotesScrollPane.minHeightProperty().bind(height);
        this.speakerNotesScrollPane.maxHeightProperty().bind(height);

        this.speakerNotesScrollPane.setTranslateX(50);
        this.speakerNotesScrollPane.translateYProperty().bind(this.heightProperty().subtract(height).subtract(50));

        this.getChildren().add(this.speakerNotesScrollPane);
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
            final LocalTime now = LocalTime.now();

            final Duration duration = Duration.between(this.beginningTime, now);
            final long numberOfHours = duration.getSeconds() / 3600;
            final long numberOfMinutes = (duration.getSeconds() % 3600) / 60;
            final long numberOfSeconds = (duration.getSeconds() % 3600) % 60;

            this.timeElapsed.setText(
                    String.format("%1$02d:%02$02d:%3$02d", numberOfHours, numberOfMinutes, numberOfSeconds)
            );

            this.currentTime.setText(String.format("%1$tH:%1$tM:%1$tS", now));
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
     * Defines the speaker notes to display.
     *
     * @param speakerNotes The speaker notes to be displayed.
     */
    public void setSpeakerNotes(final String speakerNotes) {
        this.speakerNotes.setText(speakerNotes);
    }

    /**
     * Get the browser that displays the next slide of the presentation.
     *
     * @return The browser displaying the next slide of the presentation.
     */
    public PresentationBrowser getNextSlideBrowser() {
        return nextSlideBrowser;
    }

    /**
     * Get the browser that displays the current slide of the presentation.
     *
     * @return The browser displaying the current slide of the presentation.
     */
    public PresentationBrowser getCurrentSlideBrowser() {
        return currentSlideBrowser;
    }
}
