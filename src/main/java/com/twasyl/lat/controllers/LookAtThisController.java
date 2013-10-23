package com.twasyl.lat.controllers;

import com.twasyl.lat.app.LookAtThis;
import com.twasyl.lat.scene.controls.Presentation;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LookAtThisController implements Initializable {

    @FXML private void startSlideshow(ActionEvent event) {

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Presentations", "*.fxml"));
        File presentationFile = chooser.showOpenDialog(LookAtThis.getStage().getOwner());

        if(presentationFile != null && presentationFile.exists()) {
            try {
                final Presentation presentation = FXMLLoader.load(presentationFile.toPath().toUri().toURL());
                presentation.start();

                final Scene scene = new Scene(presentation.showSlide());
                scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
                            presentation.next();
                            scene.setRoot(presentation.showSlide());
                        } else if (keyEvent.getCode().equals(KeyCode.LEFT)) {
                            presentation.previous();
                            scene.setRoot(presentation.showSlide());
                        }
                    }
                });

                LookAtThis.getStage().setScene(scene);
            } catch (IOException e) {
            }
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
