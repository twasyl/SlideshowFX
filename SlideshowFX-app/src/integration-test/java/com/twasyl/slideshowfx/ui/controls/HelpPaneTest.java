package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.controls.stages.HelpStage;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX
 */
public class HelpPaneTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        new HelpStage().show();
    }

    public static void main(String[] args) {
        HelpPaneTest.launch(args);
    }
}
