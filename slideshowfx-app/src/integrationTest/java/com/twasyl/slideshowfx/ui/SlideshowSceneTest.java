package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.slideshow.Context;
import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author Thierry Wasylczenko
 */
public class SlideshowSceneTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        final PresentationEngine presentationEngine = new PresentationEngine();
        presentationEngine.loadArchive(new File("examples/presentations/SlideshowFX.sfx"));

        final Context context = new Context();
        context.setPresentation(presentationEngine);
        context.setStartAtSlideId("slide-1427809422878");

        final SlideshowPane root = new SlideshowPane();
        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        SlideshowSceneTest.launch(args);
    }
}
