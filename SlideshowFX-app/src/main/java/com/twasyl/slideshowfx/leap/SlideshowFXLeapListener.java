/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.leap;

import com.leapmotion.leap.*;
import com.twasyl.slideshowfx.app.SlideshowFX;
import javafx.scene.input.KeyCode;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class is used the LeapMotion listener for controlling the slideshow.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowFXLeapListener extends Listener {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXLeapListener.class.getName());

    /**
     * When performing a gesture, indicates the maximum angle from the X axis the gesture's direction could have.
     */
    public static final double X_AXIS_DIRECTION_MAX_ANGLE = 25;
    /**
     * When performing a gesture, indicates the maximum angle from the Z axis the gesture's direction could have.
     */
    public static final double Z_AXIS_DIRECTION_MAX_ANGLE = 15;

    private boolean tracking = false;
    private SwipeGesture lastSwipeGesture;

    public boolean isTracking() { return tracking; }
    public void setTracking(boolean tracking) { this.tracking = tracking; }

    @Override
    public void onInit(Controller controller) {
        super.onInit(controller);
        LOGGER.finest("SlideshowFXLeapListener - onInit");
    }

    @Override
    public void onExit(Controller controller) {
        super.onExit(controller);
        LOGGER.finest("SlideshowFXLeapListener - onExit");
    }

    @Override
    public void onConnect(Controller controller) {
        super.onConnect(controller);
        LOGGER.finest("LeapMotion controller connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    }

    @Override
    public void onDisconnect(Controller controller) {
        super.onDisconnect(controller);
        LOGGER.finest("LeapMotion controller disconnected");
    }

    @Override
    public void onFrame(final Controller controller) {
        super.onFrame(controller);

        final Frame frame = controller.frame();

        if(frame.isValid()) {
            if(frame.pointables().isEmpty()) {
                SlideshowFX.getSlideShowScene().hidePointer();
            }
        }

        if(isTracking()) {

            // Manage gestures
            if(!frame.gestures().isEmpty()) {
                Gesture gesture = null;
                Iterator<Gesture> gesturesIterator = frame.gestures().iterator();

                while(gesturesIterator.hasNext()) {
                    gesture = gesturesIterator.next();

                    if(gesture.isValid()) {
                        switch(gesture.type()) {
                            case TYPE_SWIPE:
                                manageSwipe(controller, gesture);
                                break;
                            case TYPE_KEY_TAP:
                                click(controller, gesture);
                                break;
                        }
                    }
                }
            } else {
                movePointer(controller);
            }
        }
    }

    private void manageSwipe(final Controller controller, final Gesture gesture) {
        final SwipeGesture swipe = new SwipeGesture(gesture);

        if(swipe.state() == Gesture.State.STATE_START) {
            this.lastSwipeGesture = swipe;
        } else if(swipe.state() == Gesture.State.STATE_STOP) {
            // Only compute the swipe if it executed more than 1 second after the previous one

            if(this.lastSwipeGesture != null) {
                // Timestamp are in microseconds
                long lastSwipeTimestamp = this.lastSwipeGesture.frame().timestamp();
                long currentTimestamp = swipe.frame().timestamp();

                boolean acceptSwipe = swipe.durationSeconds() >= 0.1;

                System.out.println("Accept swipe ? " + (acceptSwipe) + "(duration: " + swipe.durationSeconds() + "s)");
                changeSlide(controller, swipe);

                this.lastSwipeGesture = null;
            }
        }
    }

    /**
     * Moves the pointer on the screen. In order to move it, the following criteria are check:
     * <ul>
     *     <li>One hand is visible and valid</li>
     *     <li>One finger is visible and valid</li>
     * </ul>
     * @param controller
     * @return true if the pointer has been moved.
     */
    private boolean movePointer(final Controller controller) {
        boolean pointerHasMoved = false;

        final Frame frame = controller.frame();

        if(!frame.hands().isEmpty() && frame.hands().count() == 1) {
            final Hand hand = frame.hands().get(0);

            if(hand.isValid()) {

                // Only movePointer if we find an INDEX finger and if it is extended
                if(!hand.fingers().isEmpty()) {

                    Finger finger, indexFinger = null;

                    boolean otherFingersClosed = true;
                    boolean indexFingerValid = false;

                    Iterator<Finger> fingerIterator = hand.fingers().iterator();

                    while(fingerIterator.hasNext() && otherFingersClosed) {
                        finger = fingerIterator.next();

                        if(finger.type() != Finger.Type.TYPE_INDEX) {
                            otherFingersClosed = !finger.isExtended();
                        } else {
                            indexFingerValid = finger.isExtended();
                            indexFinger = finger;
                        }
                    }

                    if(indexFingerValid && otherFingersClosed) {

                        final InteractionBox box = frame.interactionBox();
                        final Vector normalizedPosition = box.normalizePoint(indexFinger.tipPosition());

                        double screenWidth = SlideshowFX.getSlideShowScene().getWidth();
                        double screenHeight = SlideshowFX.getSlideShowScene().getHeight();

                        double computedX = normalizedPosition.getX() * screenWidth;
                        double computedY = screenHeight - (normalizedPosition.getY() * screenHeight);

                        double verticalOrientation = Math.toDegrees(indexFinger.direction().angleTo(Vector.yAxis()));
                        double yPadding = (verticalOrientation - 90) * 10;

                        double horizontalOrientation = Math.toDegrees(indexFinger.direction().angleTo(Vector.xAxis()));
                        double xPadding = (90 - horizontalOrientation) * 15;

                        if(verticalOrientation > 90) computedY += yPadding;
                        else if(verticalOrientation < 90) computedY -= yPadding;

                        if(horizontalOrientation < 90) computedX -= xPadding;
                        else if(horizontalOrientation > 90) computedX += xPadding;

                        SlideshowFX.getSlideShowScene().showPointer(computedX, computedY);
                        pointerHasMoved = true;
                    }
                }
            }
        }

        return pointerHasMoved;
    }

    /**
     * Change slide if the given gesture respect the following criteria:
     * <ul>
     *     <li>Only one hand is visible and valid</li>
     *     <li>Only two fingers are used and are valid, ie index and middle fingers are extended, others not.</li>
     *     <li>The direction of the gesture is horizontal</li>
     * </ul>
     * @param controller
     * @param gesture
     * @return true if the slide has been changed
     */
    private boolean changeSlide(final Controller controller, final SwipeGesture gesture) {
        boolean slideHasChanged = false;
        final Frame frame = controller.frame();

        // Only one hand
        if(frame.isValid() && !frame.hands().isEmpty() && frame.hands().count() == 1) {

            final Hand hand = frame.hands().get(0);

            // Only the index and middle fingers extended. All others should be closed.
            if(hand.isValid() && !hand.fingers().isEmpty()) {

                Finger finger;
                boolean fingersAreValid = true;
                boolean indexFingerValid = false;
                boolean middleFingerValid = false;

                Iterator<Finger> fingerIterator = hand.fingers().iterator();

                // Check that each finger is valid
                while(fingerIterator.hasNext() && fingersAreValid) {
                    finger = fingerIterator.next();
                    fingersAreValid = finger.isValid();

                    switch(finger.type()) {
                        case TYPE_INDEX:
                            indexFingerValid = finger.isExtended();
                            break;
                        case TYPE_MIDDLE:
                            middleFingerValid = finger.isExtended();
                            break;
                        default:
                            fingersAreValid = !finger.isExtended();
                    }
                }

                // If all good, try to change the side
                if(fingersAreValid && indexFingerValid && middleFingerValid) {

                    // The angle given by angleTo is always less or equal Pi radians (180°)
                    double degree = Math.abs(Math.toDegrees(gesture.direction().angleTo(Vector.xAxis())));

                    /* The X direction angle is valid if the angle is
                        0 >= ANGLE <= X_AXIS_DIRECTION_MAX_ANGLE
                        OR
                        180° >= ANGLE <= 180° - X_AXIS_DIRECTION_MAX_ANGLE */
                    boolean xAxisCheck = degree <= X_AXIS_DIRECTION_MAX_ANGLE || degree >= 180 - X_AXIS_DIRECTION_MAX_ANGLE;
                    LOGGER.finest(String.format("SlideshowFXLeapListener#changeSlide : direction of the gesture compared to X axis : %1$s°", degree));

                    /* The Z direction angle is valid if the angle is
                    * 90° - Z_AXIS_DIRECTION_MAX_ANGLE >= ANGLE <= 90° + Z_AXIS_DIRECTION_MAX_ANGLE*/
                    degree = Math.abs(Math.toDegrees(gesture.direction().angleTo(Vector.zAxis())));
                    boolean zAxisAcheck = (90 - Z_AXIS_DIRECTION_MAX_ANGLE) <= degree && (90 + Z_AXIS_DIRECTION_MAX_ANGLE) >= degree;
                    LOGGER.finest(String.format("SlideshowFXLeapListener#changeSlide : direction of the gesture compared to Z axis : %1$s°", degree));

                    // If the swipe is considered as horizontal
                    if(xAxisCheck && zAxisAcheck) {
                        // Check the gesture is a swipe and determine direction
                        if(gesture.direction().getX() > 0) {
                            SlideshowFX.getSlideShowScene().hidePointer();
                            SlideshowFX.getSlideShowScene().sendKey(KeyCode.LEFT);
                            slideHasChanged = true;
                        } else if(gesture.direction().getX() < 0) {
                            SlideshowFX.getSlideShowScene().hidePointer();
                            SlideshowFX.getSlideShowScene().sendKey(KeyCode.RIGHT);
                            slideHasChanged = true;
                        }
                    }
                }
            }
        }

        return slideHasChanged;
    }

    /**
     * Performs a click on the screen if the pointer is visible and the given gesture respects the following criteria:
     * <ul>
     *     <li>Only one hand is visible</li>
     *     <li>The 5 fingers of the hand are visible</li>
     * </ul>
     *
     * The click is performed where the pointer is located.
     *
     * @param controller
     * @param gesture
     * @return true if a click has been performed.
     */
    private boolean click(final Controller controller, Gesture gesture) {
        final KeyTapGesture keyTap = new KeyTapGesture(gesture);
        boolean clickPerformed = false;

        final Frame frame = controller.frame();

        // Only one hand
        if (frame.isValid() && !frame.hands().isEmpty() && frame.hands().count() == 1) {
            final Hand hand = frame.hands().get(0);

            // Five fingers
            if (hand.isValid() && !hand.fingers().isEmpty() && hand.fingers().count() == 5) {

                boolean fingersExtended = true;
                Iterator<Finger> iterator = hand.fingers().iterator();
                Finger finger;

                while(iterator.hasNext() && fingersExtended) {
                    finger = iterator.next();

                    fingersExtended = finger.isValid() && finger.isExtended();
                }

                SlideshowFX.getSlideShowScene().click();
                clickPerformed = true;
            }
        }

        return clickPerformed;
    }

}
