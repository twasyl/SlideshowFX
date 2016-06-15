package com.twasyl.slideshowfx.setup.app;

import com.twasyl.slideshowfx.setup.controllers.SetupViewController;
import com.twasyl.slideshowfx.setup.enums.SetupStatus;
import com.twasyl.slideshowfx.setup.step.*;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Application class used to perform the setup of the application on the client's computer.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SlideshowFXSetup extends Application {

    protected File pluginsDirectory;
    protected File applicationArtifact;
    protected File documentationsFolder;
    protected String applicationName;
    protected String applicationVersion;
    protected SetupViewController controller;

    protected Parent getRootNode() throws IOException {
        final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/setup/fxml/SetupView.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();

        final String license = ResourceHelper.readResource("/com/twasyl/slideshowfx/setup/license/LICENSE");

        controller.addStep(new WelcomeStep(this.applicationName, this.applicationVersion))
                .addStep(new LicenseStep(license))
                .addStep(new InstallationLocationStep(this.applicationName, this.applicationVersion, this.applicationArtifact, this.documentationsFolder))
                .addStep(new PluginsStep(this.pluginsDirectory))
                .addStep(new FinishStep(this.applicationName, this.applicationVersion));

        return root;
    }

    @Override
    public void init() throws Exception {
        super.init();

        this.pluginsDirectory = new File(System.getProperty("setup.plugins.directory"));
        this.applicationArtifact = new File(System.getProperty("setup.application.artifact"));
        this.documentationsFolder = new File(System.getProperty("setup.documentations.directory"));
        this.applicationName = System.getProperty("setup.application.name");
        this.applicationVersion = System.getProperty("setup.application.version");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(event -> {
            if(controller.getSetupStatus() == SetupStatus.IN_PROGRESS) {
                this.controller.cancelSetup();
            }

            if(this.controller.getSetupStatus() != SetupStatus.SUCCESSFUL && this.controller.getSetupStatus() != SetupStatus.ABORTED) {
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
