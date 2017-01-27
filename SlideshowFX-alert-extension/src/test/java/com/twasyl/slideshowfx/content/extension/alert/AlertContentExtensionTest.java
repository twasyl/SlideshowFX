package com.twasyl.slideshowfx.content.extension.alert;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class AlertContentExtensionTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(AlertContentExtensionTest.class.getResource("/com/twasyl/slideshowfx/content/extension/alert/fxml/AlertContentExtension.fxml"));

        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
