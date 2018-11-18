package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.theme.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Test class for the plugin center.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.1
 */
public class PluginCenterTest extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        OSGiManager.getInstance().startAndDeploy();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        OSGiManager.getInstance().stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(PluginCenterTest.class.getResource("/com/twasyl/slideshowfx/fxml/PluginCenter.fxml"));

        Themes.applyTheme(root, "Dark");
        final Scene scene = new Scene(root);
        scene.getStylesheets().add("/com/twasyl/slideshowfx/css/Default.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
