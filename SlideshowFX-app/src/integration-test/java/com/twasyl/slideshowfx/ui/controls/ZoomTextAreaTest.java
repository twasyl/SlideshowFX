package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.controls.ZoomTextArea;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @version 1.0
 */
public class ZoomTextAreaTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        final ZoomTextArea textArea = new ZoomTextArea("SlideshowFX");
        textArea.setPrefRowCount(10);
        textArea.setPrefColumnCount(50);

        final Scene scene = new Scene(textArea);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        ZoomTextAreaTest.launch(args);
    }
}
