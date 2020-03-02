package com.twasyl.slideshowfx.setup.app;

import com.twasyl.slideshowfx.setup.controllers.SetupViewController;
import com.twasyl.slideshowfx.setup.enums.SetupStatus;
import com.twasyl.slideshowfx.setup.step.*;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application class used to perform the setup of the application on the client's computer.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class SlideshowFXSetup extends Application {
    protected SetupViewController controller;

    protected Parent getRootNode() throws IOException {
        final FXMLLoader loader = new FXMLLoader(SlideshowFXSetup.class.getResource("/com/twasyl/slideshowfx/setup/fxml/SetupView.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();

        final String license = IOUtils.read(SlideshowFXSetup.class.getResourceAsStream("/com/twasyl/slideshowfx/setup/license/LICENSE"));

        controller.addStep(new WelcomeStep())
                .addStep(new LicenseStep(license))
                .addStep(new InstallationLocationStep())
                .addStep(new PluginsStep())
                .addStep(new FinishStep());

        return root;
    }

    @Override
    public void init() throws Exception {
        super.init();
        SetupProperties.getInstance();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(event -> {
            if (controller.getSetupStatus() == SetupStatus.IN_PROGRESS) {
                this.controller.cancelSetup();
            }

            if (this.controller.getSetupStatus() != SetupStatus.SUCCESSFUL && this.controller.getSetupStatus() != SetupStatus.ABORTED) {
                event.consume();
            }
        });

        final Scene scene = new Scene(this.getRootNode());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
