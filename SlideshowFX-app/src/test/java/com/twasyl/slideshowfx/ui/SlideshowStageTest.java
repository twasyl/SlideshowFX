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

package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.slideshow.Context;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowStage;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author Thierry Wasylczenko
 */
public class SlideshowStageTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final PresentationEngine presentationEngine = new PresentationEngine();
        presentationEngine.loadArchive(new File("examples/presentations/SlideshowFX.sfx"));

        final Context context = new Context();
        context.setPresentation(presentationEngine);
        context.setLeapMotionEnabled(false);
        context.setStartAtSlideId("slide-1427809422878");

        final SlideshowStage slideshowStage = new SlideshowStage(context);
        slideshowStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
