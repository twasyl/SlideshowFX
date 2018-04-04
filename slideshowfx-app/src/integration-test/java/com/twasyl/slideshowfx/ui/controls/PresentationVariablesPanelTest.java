package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.controls.PresentationVariablesPanel;
import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Collections;

/**
 * @author Thierry Wasylczenko
 */
public class PresentationVariablesPanelTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        PresentationConfiguration configuration = new PresentationConfiguration();
        configuration.setVariables(Collections.emptyList());

        final Scene scene = new Scene(new PresentationVariablesPanel(configuration));
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        PresentationVariablesPanelTest.launch(args);
    }
}
