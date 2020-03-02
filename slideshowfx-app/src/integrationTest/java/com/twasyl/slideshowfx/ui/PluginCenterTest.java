package com.twasyl.slideshowfx.ui;

import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.style.theme.Themes;
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
        PluginManager.getInstance().start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        PluginManager.getInstance().stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(PluginCenterTest.class.getResource("/com/twasyl/slideshowfx/fxml/PluginCenter.fxml"));

        Themes.applyTheme(root, "Dark");
        final Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
