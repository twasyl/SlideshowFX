/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.controls;

import com.leapmotion.leap.Controller;
import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.leap.SlideshowFXLeapListener;
import javafx.scene.input.KeyCode;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The stage is defined when the presentation enters in slideshow mode. It defines a stage with the expected behaviour
 * with LeapMotion and interaction with the keyboard. It is necessary to create the stage with a {@link SlideshowScene}
 * and indicating if LeapMotion should be enabled.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowStage extends Stage {

    private final boolean leapMotionEnabled;
    private final Controller controller;
    private final SlideshowFXLeapListener listener;

    public SlideshowStage(final SlideshowScene scene, final boolean leapMotionEnabled) {
        super(StageStyle.UNDECORATED);
        this.setScene(scene);

        // Setting the size of the stage according on which screen the main app is.
        Screen screen = Screen.getScreensForRectangle(SlideshowFX.getStage().getX(),
                SlideshowFX.getStage().getY(),
                SlideshowFX.getStage().getWidth(),
                SlideshowFX.getStage().getHeight()).get(0);

        this.setX(screen.getBounds().getMinX());
        this.setY(screen.getBounds().getMinY());
        this.setWidth(screen.getBounds().getWidth());
        this.setHeight(screen.getBounds().getHeight());

        /**
         * LeapMotion initialization
         */
        this.leapMotionEnabled = leapMotionEnabled;
        if(this.leapMotionEnabled) {
            this.listener = new SlideshowFXLeapListener(scene);
            this.listener.setTracking(true);
            this.controller = new Controller();
        } else {
            this.listener = null;
            this.controller = null;
        }

        this.setOnShowing(event -> {
            if(this.leapMotionEnabled) {
                this.controller.addListener(this.listener);
            }
        });

        this.setOnCloseRequest(event -> {
             if(this.leapMotionEnabled) {
                 this.controller.removeListener(this.listener);
             }
        });

        scene.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                keyEvent.consume();

                scene.exitSlideshow();

                this.close();
            } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                scene.click();
            }
        });

        this.setAlwaysOnTop(true);
    }
}
