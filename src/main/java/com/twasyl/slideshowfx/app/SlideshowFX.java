package com.twasyl.slideshowfx.app;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SlideshowFX extends Application {

    private static final ReadOnlyObjectProperty<Stage> stage = new SimpleObjectProperty<>();

    public static ReadOnlyObjectProperty<Stage> stageProperty() {
        return stage;
    }

    public static Stage getStage() {
        return stageProperty().get();
    }

    public static ReadOnlyObjectProperty<Scene> presentationBuilderScene = new SimpleObjectProperty<>();

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

    public static void main(String[] args) {
        launch(args);
    }
}
