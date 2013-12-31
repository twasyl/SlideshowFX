package com.twasyl.slideshowfx.app;

import com.leapmotion.leap.Controller;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.leap.SlideController;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class SlideshowFX extends Application {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFX.class.getName());
    private static final ReadOnlyObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private static final ReadOnlyObjectProperty<Scene> presentationBuilderScene = new SimpleObjectProperty<>();
    private static final ObjectProperty<SlideShowScene> slideShowScene = new SimpleObjectProperty<>();
    private static final BooleanProperty slideShowActive = new SimpleBooleanProperty(false);

    private static Controller leapController;
    private static SlideController slideController;


    @Override
    public void init() throws Exception {
        // Init LeapMotion
        slideController = new SlideController();
        leapController = new Controller();

        // The listener is added and removed each time the slideShowActive property changes
        slideShowActiveProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                if(aBoolean2) {
                    leapController.addListener(slideController);
                } else {
                    leapController.removeListener(slideController);

                    getStage().setScene(presentationBuilderScene.get());
                    getStage().setFullScreen(false);
                }
            }
        });

        // Init the slideshow scene
        slideShowSceneProperty().addListener(new ChangeListener<SlideShowScene>() {
            @Override
            public void changed(ObservableValue<? extends SlideShowScene> observableValue, SlideShowScene scene, SlideShowScene scene2) {
                if(scene2 != null) {
                    getStage().setScene(scene2);
                    getStage().setFullScreen(true);

                    SlideshowFX.setSlideShowActive(true);
                }
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        ((SimpleObjectProperty<Stage>) SlideshowFX.stage).set(stage);

        final Parent root = FXMLLoader.load(getClass().getResource("/com/twasyl/slideshowfx/fxml/SlideshowFX.fxml"));

        final Scene scene = new Scene(root);
        ((SimpleObjectProperty<Scene>) presentationBuilderScene).set(scene);

        stage.setTitle("SlideshowFX");
        stage.setScene(scene);

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        LOGGER.info("Stopping the LeapMotion controller correctly");
        leapController.removeListener(slideController);
    }

    public static ReadOnlyObjectProperty<Stage> stageProperty() { return stage; }
    public static Stage getStage() { return stageProperty().get(); }

    public static final BooleanProperty slideShowActiveProperty() { return slideShowActive; }
    public static final Boolean isSlideShowActive() { return slideShowActiveProperty().get(); }
    public static final void setSlideShowActive(boolean active) { slideShowActiveProperty().set(active); }

    public static final ObjectProperty<SlideShowScene> slideShowSceneProperty() { return slideShowScene; }
    public static final SlideShowScene getSlideShowScene() { return slideShowSceneProperty().get(); }
    public static final void setSlideShowScene(SlideShowScene scene) { slideShowSceneProperty().set(scene); }

    public static void main(String[] args) {
        launch(args);
    }
}
