package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.controls.stages.AboutStage;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX
 */
public class AboutStageTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        new AboutStage().show();
    }

    public static void main(String[] args) {
        AboutStageTest.launch(args);
    }
}
