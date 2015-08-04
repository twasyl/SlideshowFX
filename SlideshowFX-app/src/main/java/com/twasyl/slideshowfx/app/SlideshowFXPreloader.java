/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.app;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * This class is the custom preloader for SlideshowFX. I t displays a splash screen that fade in and fade out
 * before the application starts.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowFXPreloader extends Preloader {

    private Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.currentStage = primaryStage;

        final Image splashImage = new Image(getClass().getResourceAsStream("/com/twasyl/slideshowfx/images/splash.png"));
        final ImageView view = new ImageView(splashImage);

        final BorderPane pane = new BorderPane();
        pane.centerProperty().set(view);
        pane.setBackground(null);
        pane.setOpacity(0);

        final Scene scene = new Scene(pane);
        scene.setFill(null);

        this.currentStage.initStyle(StageStyle.TRANSPARENT);
        this.currentStage.setScene(scene);
        this.currentStage.getIcons().addAll(
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/512.png")));
        this.currentStage.show();

        final FadeTransition fadeIn = new FadeTransition(Duration.millis(500), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if(info.getType() == StateChangeNotification.Type.BEFORE_START) {
            final FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this.currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(vent -> this.currentStage.hide());
            fadeOut.play();
        }
    }
}
