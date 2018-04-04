package com.twasyl.slideshowfx.app;

import com.twasyl.slideshowfx.utils.Jar;
import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the custom preloader for SlideshowFX. It displays a splash screen that fade in and fade out
 * before the application starts.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideshowFXPreloader extends Preloader {
    private static Logger LOGGER = Logger.getLogger(SlideshowFXPreloader.class.getName());
    private Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.currentStage = primaryStage;

        final StackPane pane = getRootView();
        final Scene scene = new Scene(pane);
        scene.setFill(null);

        this.currentStage.initStyle(StageStyle.TRANSPARENT);
        this.currentStage.setScene(scene);
        this.currentStage.getIcons().addAll(
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/512.png")));
        this.currentStage.show();

        final FadeTransition fadeIn = new FadeTransition(Duration.millis(500), pane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    protected StackPane getRootView() {
        final StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setBackground(null);
        pane.setOpacity(0);

        final Label version = getVersion();
        version.setTranslateY(110);

        pane.getChildren().addAll(getSplashImage(), version);

        return pane;
    }

    protected ImageView getSplashImage() {
        final Image splashImage = new Image(SlideshowFXPreloader.class.getResourceAsStream("/com/twasyl/slideshowfx/images/splash.png"));
        return new ImageView(splashImage);
    }

    protected Label getVersion() {
        final Font font = new Font(Font.getDefault().getName(), 15);

        final Label text = new Label();
        text.setFont(font);

        try {
            try (final Jar jar = Jar.fromClass(getClass())) {
                text.setText(jar.getImplementationVersion());
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not determine application version", e);
        }

        return text;
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            final FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this.currentStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(vent -> this.currentStage.hide());
            fadeOut.play();
        }
    }
}
