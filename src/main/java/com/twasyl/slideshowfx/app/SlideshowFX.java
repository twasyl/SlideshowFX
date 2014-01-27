package com.twasyl.slideshowfx.app;

import com.leapmotion.leap.Controller;
import com.twasyl.slideshowfx.chat.Chat;
import com.twasyl.slideshowfx.controls.SlideShowScene;
import com.twasyl.slideshowfx.leap.SlideController;
import com.twasyl.slideshowfx.utils.NetworkUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.http.impl.WebSocketMatcher;
import org.vertx.java.core.streams.Pump;
import org.w3c.dom.ls.LSOutput;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

public class SlideshowFX extends Application {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFX.class.getName());
    private static final ReadOnlyObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private static final ReadOnlyObjectProperty<Scene> presentationBuilderScene = new SimpleObjectProperty<>();
    private static final ObjectProperty<SlideShowScene> slideShowScene = new SimpleObjectProperty<>();
    private static final BooleanProperty slideShowActive = new SimpleBooleanProperty(false);
    private static final BooleanProperty leapMotionAllowed = new SimpleBooleanProperty();

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
                if (aBoolean2) {
                    leapController.addListener(slideController);
                } else {
                    leapController.removeListener(slideController);

                    getStage().setScene(presentationBuilderScene.get());
                    getStage().setFullScreen(false);
                }
            }
        });

        // LeapMotion controller should track gestures if it is enabled by the application and the slideshow is active
        leapMotionAllowedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                slideController.setTracking(aBoolean2);
                LOGGER.finest(String.format("LeapMotion tracking has changed to %1$s", aBoolean2));
            }
        });

        // Init the slideshow scene
        slideShowSceneProperty().addListener(new ChangeListener<SlideShowScene>() {
            @Override
            public void changed(ObservableValue<? extends SlideShowScene> observableValue, SlideShowScene scene, SlideShowScene scene2) {
                if (scene2 != null) {
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

        Chat.close();
    }

    public static ReadOnlyObjectProperty<Stage> stageProperty() { return stage; }
    public static Stage getStage() { return stageProperty().get(); }

    public static final BooleanProperty slideShowActiveProperty() { return slideShowActive; }
    public static final Boolean isSlideShowActive() { return slideShowActiveProperty().get(); }
    public static final void setSlideShowActive(boolean active) { slideShowActiveProperty().set(active); }

    public static BooleanProperty leapMotionAllowedProperty() { return leapMotionAllowed; }
    public static boolean isLeapMotionAllowed() { return leapMotionAllowedProperty().get(); }
    public static void setLeapMotionAllowed(boolean leapMotionAllowed) { leapMotionAllowedProperty().set(leapMotionAllowed); }

    public static final ObjectProperty<SlideShowScene> slideShowSceneProperty() { return slideShowScene; }
    public static final SlideShowScene getSlideShowScene() { return slideShowSceneProperty().get(); }
    public static final void setSlideShowScene(SlideShowScene scene) { slideShowSceneProperty().set(scene); }

    public static void main(String[] args) {
        launch(args);
    }
}
