package com.twasyl.slideshowfx.leap;

import com.leapmotion.leap.*;
import com.sun.javafx.scene.input.KeyCodeMap;
import com.twasyl.slideshowfx.app.SlideshowFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class SlideController extends Listener {

    private static final Logger LOGGER = Logger.getLogger(SlideController.class.getName());

    private boolean tracking = false;

    public boolean isTracking() { return tracking; }
    public void setTracking(boolean tracking) { this.tracking = tracking; }

    @Override
    public void onInit(Controller controller) {
        super.onInit(controller);
        LOGGER.finest("SlideController - onInit");
    }

    @Override
    public void onExit(Controller controller) {
        super.onExit(controller);
        LOGGER.finest("SlideController - onExit");
    }

    @Override
    public void onConnect(Controller controller) {
        super.onConnect(controller);
        LOGGER.finest("LeapMotion controller connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
    }

    @Override
    public void onDisconnect(Controller controller) {
        super.onDisconnect(controller);
        LOGGER.finest("LeapMotion controller disconnected");
    }

    @Override
    public void onFrame(final Controller controller) {
        super.onFrame(controller);

        if(isTracking()) {
            final Frame frame = controller.frame();

            // Get a swipe
            Gesture gesture = null;
            Iterator<Gesture> gesturesIterator = frame.gestures().iterator();

            while(gesturesIterator.hasNext()) {
                gesture = gesturesIterator.next();

                if(gesture.isValid()) {
                    switch(gesture.type()) {
                        case TYPE_SWIPE:
                            changeSlide(controller, gesture);
                            break;
                    }
                }

            }
        }
    }

    private void changeSlide(final Controller controller, final Gesture gesture) {
        final Frame frame = controller.frame();

        SwipeGesture swipe = new SwipeGesture(gesture);

        // The gesture is finished
        if(swipe.state() == Gesture.State.STATE_STOP) {

            if(!frame.hands().isEmpty() && frame.hands().count() == 1) {
                final Hand hand = frame.hands().get(0);

                if(hand.isValid()) {

                    // Only allow index and major fingers
                    if(!hand.fingers().isEmpty() && hand.fingers().count() == 2) {
                        boolean swipeValid = true;

                        Iterator<Finger> fingerIterator = hand.fingers().iterator();

                        // Check that each finger is valid
                        while(fingerIterator.hasNext() && swipeValid) {
                            swipeValid = fingerIterator.next().isValid();
                        }

                        if(swipeValid) {

                            // Check the gesture is a swipe and determine direction
                            if(swipe.direction().getX() > 0) {
                                SlideshowFX.getSlideShowScene().sendKey(KeyCode.LEFT);
                            } else if(swipe.direction().getX() < 0) {
                                SlideshowFX.getSlideShowScene().sendKey(KeyCode.RIGHT);
                            }
                        }
                    }
                }
            }
        }
    }
}
