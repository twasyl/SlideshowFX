package com.twasyl.slideshowfx.controls.builder;

import com.twasyl.slideshowfx.controls.builder.nodes.TemplateConfigurationFilePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.3
 */
public class ConfigurationFilePaneTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TemplateConfigurationFilePane templateConfigurationFilePane = new TemplateConfigurationFilePane();
        final ScrollPane scrollPane = new ScrollPane(templateConfigurationFilePane);

        final Scene scene = new Scene(scrollPane, 500, 600);
        scene.getStylesheets().add(
                ConfigurationFilePaneTest.class.getResource("/com/twasyl/slideshowfx/css/application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
