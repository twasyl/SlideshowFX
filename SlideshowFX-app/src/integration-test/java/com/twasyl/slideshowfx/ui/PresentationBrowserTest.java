package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.controls.PresentationBrowser;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author Thierry Wasylczenko
 */
public class PresentationBrowserTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final PresentationEngine presentationEngine = new PresentationEngine();
        presentationEngine.loadArchive(new File("examples/presentations/SlideshowFX.sfx"));

        final PresentationBrowser browser = new PresentationBrowser();
        browser.loadPresentation(presentationEngine);

        final Scene scene = new Scene(browser, 1280, 900);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
