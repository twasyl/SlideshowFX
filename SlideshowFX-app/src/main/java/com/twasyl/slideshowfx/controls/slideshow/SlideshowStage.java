/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.slideshow;

import com.leapmotion.leap.Controller;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.leap.SlideshowFXLeapListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.logging.Logger;

/**
 * The stage is defined when the presentation enters in slideshow mode. It defines a stage with the expected behaviour
 * with LeapMotion and interaction with the keyboard. It is necessary to create the stage with a {@link Context}
 * to properly configure the stage.
 * The stage will take care of the creation of the necessary screens (slideshow and information screens) as well as
 * defining to which event screen will respond (key event and so on).
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowStage {
    private static final Logger LOGGER = Logger.getLogger(SlideshowStage.class.getName());
    private static final String DO_NOT_CONSIDER_EVENT_TEXT = "do_not_consider";
    private Context context;
    private Controller controller;
    private SlideshowFXLeapListener listener;

    private Runnable onCloseAction = null;
    private Stage slideshowStage, informationStage;
    private SlideshowPane slideshowPane;
    private InformationPane informationPane;

    /**
     * Creates a stage according the configuration stored within the {@code context}.
     * @param context The context defining the configuration of the stage.
     */
    public SlideshowStage(final Context context) {
        this.context = context;

        this.initializeSlideshowStage();
        this.initializeInformationStage();
        this.initializeLeapMotion();

        this.initializeKeyEvents();
    }

    /**
     * Initializes the stage that will host the presentation itself. This stage will always be created and displayed.
     * If the computer of the user has more than one screen, the stage will be displayed on the first secondary screen
     * found, which should be a video projector for instance. If only one screen is present, the stage will be displayed
     * on it.
     */
    private final void initializeSlideshowStage() {
        this.slideshowPane = new SlideshowPane(this.context);

        final Scene scene = new Scene(this.slideshowPane);

        this.slideshowStage = new Stage(StageStyle.UNDECORATED);
        this.slideshowStage.setScene(scene);

        /*
        Getting the number of screens in order to place the stage for the presentation on the right one. If there is
        more than one screen, then the slideshow is displayed on the first secondary screen in order to put the
        information stage on the primary one. If there is only one screen then the slideshow will be displayed on it.
         */
        final int numberOfScreens = Screen.getScreens().size();
        Screen screenToDisplayOn = null;

        if(numberOfScreens == 1) {
            screenToDisplayOn = Screen.getPrimary();
        } else {
            final Screen primary = Screen.getPrimary();
            int index = 0;

            do {
                if(!Screen.getScreens().get(index).equals(primary))    {
                    screenToDisplayOn = Screen.getScreens().get(index);
                }
                index++;
            } while(screenToDisplayOn == null && index < numberOfScreens);
        }

        if(screenToDisplayOn != null) {
            this.slideshowStage.setX(screenToDisplayOn.getBounds().getMinX());
            this.slideshowStage.setY(screenToDisplayOn.getBounds().getMinY());
            this.slideshowStage.setWidth(screenToDisplayOn.getBounds().getWidth());
            this.slideshowStage.setHeight(screenToDisplayOn.getBounds().getHeight());
        }

        this.slideshowStage.setAlwaysOnTop(true);
    }

    /**
     * Initializes the stage that will host the information (time elapsed since the beginning of the presentation,
     * current slide, next slide) of the presentation. This stage will only be created if more than one screen is present
     * on the user computer. The information screen will be displayed on the primary screen.
     */
    private final void initializeInformationStage() {
        /*
        The information screen is only shown if there are more than one screen detected. The information screen is
        displayed on the primary screen.
         */
        final int numberOfScreens = Screen.getScreens().size();

        if(numberOfScreens > 1) {
            final Screen screenToDisplayOn = Screen.getPrimary();

            this.informationPane = new InformationPane(this.context);

            final Scene scene = new Scene(this.informationPane);

            this.informationStage = new Stage(StageStyle.UNDECORATED);
            this.informationStage.setX(screenToDisplayOn.getBounds().getMinX());
            this.informationStage.setY(screenToDisplayOn.getBounds().getMinY());
            this.informationStage.setWidth(screenToDisplayOn.getBounds().getWidth());
            this.informationStage.setHeight(screenToDisplayOn.getBounds().getHeight());
            this.informationStage.setScene(scene);
            this.informationStage.setAlwaysOnTop(true);

            this.informationStage.setOnCloseRequest(event -> this.informationPane.stop());
            this.informationStage.setOnShowing(event -> this.informationPane.start());
        }
    }

    /**
     * Initialize LeapMotion for this stage. It creates a LeapMotion {@link com.leapmotion.leap.Listener} as well as a
     * {@link Controller} if the {@link Context#isLeapMotionEnabled()} returns {@code true}.
     */
    private final void initializeLeapMotion() {
        if(this.context.isLeapMotionEnabled()) {
            this.listener = new SlideshowFXLeapListener(this.slideshowPane);
            this.listener.setTracking(true);
            this.controller = new Controller();
        } else {
            this.listener = null;
            this.controller = null;
        }

        this.slideshowStage.setOnShowing(event -> {
            if (this.context.isLeapMotionEnabled()) {
                this.controller.addListener(this.listener);
            }
        });

        this.slideshowStage.setOnCloseRequest(event -> {
            if (this.context.isLeapMotionEnabled()) {
                this.controller.removeListener(this.listener);
            }
        });
    }

    /**
     * Set the management of {@link KeyEvent} fired within the screens and browsers displayed. This allows to define the
     * communication of events between the {@link #slideshowPane} and the {@link #informationPane} meaning that when a
     * key is pressed in one or other of these screens, a {@link KeyEvent} is fired to the other one in order display
     * the correct slide in each {@link com.twasyl.slideshowfx.controls.PresentationBrowser}.
     */
    private final void initializeKeyEvents() {
        EventHandler<KeyEvent> handler = event -> {
            if (event.getCode().equals(KeyCode.ESCAPE)) {
                if(this.onCloseAction != null) this.onCloseAction.run();

                this.slideshowStage.close();
                if (this.informationStage != null) this.informationStage.close();
            } else if(this.informationPane != null && !DO_NOT_CONSIDER_EVENT_TEXT.equals(event.getText())) {
                final KeyEvent copiedEvent = this.copyEventWithNewText(event, DO_NOT_CONSIDER_EVENT_TEXT);

                final boolean sendToInformation = event.getSource() == this.slideshowPane.getBrowser().getInternalBrowser()
                        || event.getSource() == this.informationPane.getScene();
                final boolean sendToPresentation = event.getSource() == this.informationPane.getScene();

                if(sendToInformation) {
                    this.informationPane.getCurrentSlideBrowser().setInteractionAllowed(true);
                    this.informationPane.getCurrentSlideBrowser().getInternalBrowser().fireEvent(copiedEvent);
                    this.informationPane.getCurrentSlideBrowser().setInteractionAllowed(false);

                    this.informationPane.getNextSlideBrowser().setInteractionAllowed(true);
                    this.informationPane.getNextSlideBrowser().getInternalBrowser().fireEvent(copiedEvent);
                    this.informationPane.getNextSlideBrowser().setInteractionAllowed(false);
                }

                if(sendToPresentation) this.slideshowPane.getBrowser().getInternalBrowser().fireEvent(copiedEvent);
            }
        };

        this.slideshowPane.getBrowser().getInternalBrowser().addEventHandler(KeyEvent.KEY_PRESSED, handler);
        if(this.informationPane != null) this.informationPane.getScene().addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

    /**
     * Copy a given {@code event} and set its text with a given {@code newText}. All other parameters of the original
     * event are kept.
     * @param event The event to copy.
     * @param newText The new text of the event.
     * @return A copy of the original event with a new text.
     */
    private KeyEvent copyEventWithNewText(final KeyEvent event, final String newText) {
        final KeyEvent copy = new KeyEvent(event.getSource(), event.getTarget(), event.getEventType(),
                event.getCharacter(), newText, event.getCode(), event.isShiftDown(), event.isControlDown(),
                event.isAltDown(), event.isMetaDown());

        return copy;
    }

    /**
     * Displays the slideshow and the information screen if it was previously created.
     */
    public void show() {
        if(this.slideshowStage != null) {
            this.slideshowPane.getBrowser().loadPresentationAndDo(this.context.getPresentation(), () -> {
                this.slideshowPane.getBrowser().slide(this.context.getStartAtSlideId());

                if(this.informationPane != null) {
                    this.informationPane.getCurrentSlideBrowser().loadPresentationAndDo(this.context.getPresentation(), () -> {
                        this.informationPane.getCurrentSlideBrowser().slide(this.context.getStartAtSlideId());

                        this.informationPane.getNextSlideBrowser().loadPresentationAndDo(this.context.getPresentation(), () -> {
                            final Slide startingSlide = this.context.getPresentation().getConfiguration().getSlideById(this.context.getStartAtSlideId());
                            final int indexOfSlide = this.context.getPresentation().getConfiguration().getSlides().indexOf(startingSlide);

                            Slide nextSlide = null;
                            if(indexOfSlide != -1 && indexOfSlide < this.context.getPresentation().getConfiguration().getSlides().size() - 1) {
                                nextSlide = this.context.getPresentation().getConfiguration().getSlides().get(indexOfSlide + 1);
                            } else {
                                // Go to last slide
                                nextSlide = this.context.getPresentation().getConfiguration().getLastSlide();
                            }
                            this.informationPane.getNextSlideBrowser().slide(nextSlide.getId());
                        });
                    });
                }
            });
            this.slideshowStage.show();
        }

        if(this.informationStage != null) this.informationStage.show();
    }

    /**
     * Defines the process that is executed when the stage is closed. The action is given as a {@link Runnable} object
     * but a new thread will not be created for running it. The type is just for having an interface which can describe
     * a process. The {@link Runnable#run()} method is called directly.
     * @param action The action to perform when the stage is closed.
     */
    public void onClose(Runnable action) {
        this.onCloseAction = action;
    }

    /**
     * Get the ID of the displayed slide.
     * @return The ID of the current slide or {@code null} if no slide is considered displayed.
     */
    public String getDisplayedSlideId() {
        if(this.slideshowStage != null) {
            return this.slideshowPane.getBrowser().getCurrentSlideId();
        }
        return null;
    }
}
