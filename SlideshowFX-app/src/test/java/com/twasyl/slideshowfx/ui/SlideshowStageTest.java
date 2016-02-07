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
