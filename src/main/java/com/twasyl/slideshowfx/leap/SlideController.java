package com.twasyl.slideshowfx.leap;

import com.leapmotion.leap.*;
import com.sun.javafx.scene.input.KeyCodeMap;
import com.twasyl.slideshowfx.app.SlideshowFX;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class SlideController extends Listener {

    private static final Logger LOGGER = Logger.getLogger(SlideController.class.getName());

    @Override
    public void onInit(Controller controller) {
        super.onInit(controller);
        LOGGER.info("SlideController - onInit");
    }

    @Override
    public void onExit(Controller controller) {
        super.onExit(controller);
        LOGGER.info("SlideController - onExit");
    }

    @Override
    public void onConnect(Controller controller) {
        super.onConnect(controller);
        LOGGER.info("LeapMotion controller connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
    }

    @Override
    public void onDisconnect(Controller controller) {
        super.onDisconnect(controller);
        LOGGER.info("LeapMotion controller disconnected");
    }

    @Override
    public void onFrame(Controller controller) {
        super.onFrame(controller);

        final Frame frame = controller.frame();

        if(!frame.hands().isEmpty()) {
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
                           // vers la gauche INFO: Swipe direction: (-0.680289, 0.091465, 0.727215)
                                  // vers la droite: INFO: Swipe direction: (0.978042, -0.0252404, -0.206874)
                    if(swipeValid) {
                        // Check the gesture is a swipe and determine direction
                        if(!frame.gestures().isEmpty()) {

                            // Get a swipe
                            boolean swipeFound = false;
                            Gesture gesture = null;
                            Iterator<Gesture> gesturesIterator = frame.gestures().iterator();

                            while(gesturesIterator.hasNext() && !swipeFound) {
                                gesture = gesturesIterator.next();
                                swipeFound = gesture.isValid() && gesture.type() == Gesture.Type.TYPE_SWIPE;
                            }

                            if(swipeFound) {
                                SwipeGesture swipe = new SwipeGesture(gesture);

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
}
