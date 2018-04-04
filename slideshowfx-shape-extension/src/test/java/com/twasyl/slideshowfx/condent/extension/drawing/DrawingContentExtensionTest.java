package com.twasyl.slideshowfx.condent.extension.drawing;

import com.twasyl.slideshowfx.content.extension.shape.ShapeContentExtension;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class DrawingContentExtensionTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final ShapeContentExtension extension = new ShapeContentExtension();

        final Scene scene = new Scene(extension.getUI());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
