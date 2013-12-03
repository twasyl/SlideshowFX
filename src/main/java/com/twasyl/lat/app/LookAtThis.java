package com.twasyl.lat.app;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LookAtThis extends Application {

    private static final ReadOnlyObjectProperty<Stage> stage = new SimpleObjectProperty<>();

    public static ReadOnlyObjectProperty<Stage> stageProperty() { return stage; }
    public static Stage getStage() { return stageProperty().get(); }

    public static ReadOnlyObjectProperty<Scene> presentationBuilderScene = new SimpleObjectProperty<>();

    @Override
    public void start(Stage stage) throws Exception {
        ((SimpleObjectProperty<Stage>) LookAtThis.stage).set(stage);

        final Parent root = FXMLLoader.load(getClass().getResource("/com/twasyl/lat/fxml/LookAtThis.fxml"));

        final Scene scene = new Scene(root);
        ((SimpleObjectProperty<Scene>) presentationBuilderScene).set(scene);

        stage.setScene(scene);
        stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                if(!aBoolean2) {
                    LookAtThis.getStage().setScene(LookAtThis.presentationBuilderScene.get());
                }
            }
        });
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
