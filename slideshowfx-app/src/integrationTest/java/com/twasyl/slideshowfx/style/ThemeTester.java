package com.twasyl.slideshowfx.style;

import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ThemeTester extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("/com/twasyl/slideshowfx/theme/ThemeTester.fxml"));
        Styles.applyApplicationStyle(root);
        Themes.applyTheme(root, "Light");

        final Scene scene = new Scene(root);
        primaryStage.setTitle("Theme tester");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
