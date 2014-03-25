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

        // The gesture is finished
        if(swipe.state() == Gesture.State.STATE_STOP) {

            // Only compute the swipe if it executed more than 1 second after the previous one
            boolean computeGesture = this.lastSwipeGesture == null;
            if(!computeGesture) {
                // Timestamp are in microseconds
                long lastSwipeTimestamp = this.lastSwipeGesture.frame().timestamp();
                long currentTimestamp = swipe.frame().timestamp();

                computeGesture = (lastSwipeTimestamp + 1000000l) <= currentTimestamp;
            }


            if(computeGesture) {
                changeSlide(controller, swipe);
            }

            this.lastSwipeGesture = swipe;
        }
    }

    /**
     * Moves the pointer on the screen. In order to move it, the following criterias are check:
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

                // Only movePointer if one finger
                if(!hand.fingers().isEmpty() && hand.fingers().count() == 1) {
                    final Finger finger = hand.fingers().get(0);

                    if(finger.isValid()) {
                        final InteractionBox box = frame.interactionBox();
                        final Vector normalizedPosition = box.normalizePoint(finger.tipPosition());

                        double screenWidth = SlideshowFX.getSlideShowScene().getWidth();
                        double screenHeight = SlideshowFX.getSlideShowScene().getHeight();

                        double computedX = normalizedPosition.getX() * screenWidth;
                        double computedY = screenHeight - (normalizedPosition.getY() * screenHeight);

                        double verticalOorientation = Math.toDegrees(finger.direction().angleTo(Vector.yAxis()));
                        double yPadding = (verticalOorientation - 90) * 10;

                        double horizontalOrientation = Math.toDegrees(finger.direction().angleTo(Vector.xAxis()));
                        double xPadding = (90 - horizontalOrientation) * 15;

                        if(verticalOorientation > 90) computedY += yPadding;
                        else if(verticalOorientation < 90) computedY -= yPadding;

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
     * Change slide if the given gesture respect the following criterias:
     * <ul>
     *     <li>Only one hand is visible and valid</li>
     *     <li>Only two fingers are used and are valid</li>
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

            // Only two fingers
            if(hand.isValid() && !hand.fingers().isEmpty() && hand.fingers().count() == 2) {

                boolean fingersAreValid = true;
                Iterator<Finger> fingerIterator = hand.fingers().iterator();

                // Check that each finger is valid
                while(fingerIterator.hasNext() && fingersAreValid) {
                    fingersAreValid = fingerIterator.next().isValid();
                }

                // If all good, try to change the side
                if(fingersAreValid) {

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
}
