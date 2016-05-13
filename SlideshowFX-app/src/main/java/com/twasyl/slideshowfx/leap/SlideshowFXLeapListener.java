package com.twasyl.slideshowfx.leap;

import com.leapmotion.leap.*;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import javafx.scene.input.KeyCode;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used the LeapMotion listener for controlling the slideshow. It has to be created using a
 * {@link SlideshowPane} instance with which LeapMotion will interact.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowFXLeapListener extends Listener {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXLeapListener.class.getName());

    private final SlideshowPane scene;

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

    /**
     * Creates a LeapMotion listener that will apply on a {@link SlideshowPane}. It will be used to change slides, show a pointer
     * and click on the presentation using LeapMotion.
     *
     * @param scene The âne the listener will work on.
     * @throws java.lang.NullPointerException If the given {@code scene} is null.
     */
    public SlideshowFXLeapListener(final SlideshowPane scene) {
        super();
        this.scene = scene;
    }

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
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
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
                this.scene.hidePointer();
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
                            case TYPE_SCREEN_TAP:
                                click(controller, gesture);
                                break;
                            default:
                                LOGGER.log(Level.INFO, "The gesture is not supported: " + gesture.type());
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
                boolean acceptSwipe = swipe.durationSeconds() >= 0.1;

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

        if (!frame.hands().isEmpty() && frame.hands().count() == 1) {
            final Hand hand = frame.hands().get(0);

            if (hand.isValid()) {

                FingerList extendedFingers = hand.fingers().extended();
                // Ensure we only have one extended finger
                if (extendedFingers.count() == 1) {
                    Pointable pointable = extendedFingers.get(0);

                    // We just want the index finger
                    if (pointable.isValid() && pointable.isFinger()
                            && ((Finger) pointable).type() == Finger.Type.TYPE_INDEX) {

                        final Finger indexFinger = (Finger) pointable;
                        final InteractionBox box = frame.interactionBox();

                        Vector normalizedPosition = box.normalizePoint(indexFinger.stabilizedTipPosition());
                        normalizedPosition = normalizedPosition.times(1.2f); // Scale the movement

                        double screenWidth = this.scene.getWidth();
                        double screenHeight = this.scene.getHeight();

                        double computedX = normalizedPosition.getX() * screenWidth;
                        double computedY = (1 - normalizedPosition.getY()) * screenHeight;

                        this.scene.showPointer(computedX, computedY);
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
     *     <li>Only the index and middle fingers are used and are valid</li>
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
            if(hand.isValid()) {

                final FingerList extendedFingers = hand.fingers().extended();
                if (extendedFingers.count() == 2) {
                    final Finger indexFinger = extendedFingers.fingerType(Finger.Type.TYPE_INDEX).get(0);
                    final Finger middleFinger = extendedFingers.fingerType(Finger.Type.TYPE_MIDDLE).get(0);

                    if (indexFinger != null && indexFinger.isValid()
                            && middleFinger != null && middleFinger.isValid()) {

                        // The angle given by angleTo is always less or equal Pi radians (180°)
                        double degree = Math.abs(Math.toDegrees(gesture.direction().angleTo(Vector.xAxis())));

                        /*
                         * The X direction angle is valid if the angle is
                         * 0 >= ANGLE <= X_AXIS_DIRECTION_MAX_ANGLE
                         * OR
                         * 180° >= ANGLE <= 180° - X_AXIS_DIRECTION_MAX_ANGLE
                         */
                        boolean xAxisCheck = degree <= X_AXIS_DIRECTION_MAX_ANGLE || degree >= 180 - X_AXIS_DIRECTION_MAX_ANGLE;
                        LOGGER.finest(String.format("SlideshowFXLeapListener#changeSlide : direction of the gesture compared to X axis : %1$s°", degree));

                        /*
                         * The Z direction angle is valid if the angle is
                         * 90° - Z_AXIS_DIRECTION_MAX_ANGLE >= ANGLE <= 90° + Z_AXIS_DIRECTION_MAX_ANGLE
                         */
                        degree = Math.abs(Math.toDegrees(gesture.direction().angleTo(Vector.zAxis())));
                        boolean zAxisAcheck = (90 - Z_AXIS_DIRECTION_MAX_ANGLE) <= degree && (90 + Z_AXIS_DIRECTION_MAX_ANGLE) >= degree;
                        LOGGER.finest(String.format("SlideshowFXLeapListener#changeSlide : direction of the gesture compared to Z axis : %1$s°", degree));

                        // If the swipe is considered as horizontal
                        if (xAxisCheck && zAxisAcheck) {
                            // Check the gesture is a swipe and determine direction
                            if (gesture.direction().getX() > 0) {
                                this.scene.hidePointer();
                                this.scene.sendKey(KeyCode.LEFT);
                                slideHasChanged = true;
                            } else if (gesture.direction().getX() < 0) {
                                this.scene.hidePointer();
                                this.scene.sendKey(KeyCode.RIGHT);
                                slideHasChanged = true;
                            }
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
        boolean clickPerformed = false;

        final Frame frame = controller.frame();

        // Only one hand
        if (frame.isValid() && !frame.hands().isEmpty() && frame.hands().count() == 1) {
            final Hand hand = frame.hands().get(0);

            // Only the index finger must be extended
            FingerList extendedFingers = hand.fingers().extended();
            Finger indexFinger = extendedFingers.fingerType(Finger.Type.TYPE_INDEX).get(0);

            if(extendedFingers.count() == 1 && indexFinger != null && indexFinger.isValid()) {
                System.out.println("Click");
                this.scene.click();
                clickPerformed = true;
            }
        }

        return clickPerformed;
    }

}
