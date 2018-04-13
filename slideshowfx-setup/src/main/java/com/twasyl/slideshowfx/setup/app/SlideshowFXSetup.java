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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application class used to perform the setup of the application on the client's computer.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class SlideshowFXSetup extends Application {

    protected static final String SETUP_PLUGINS_DIRECTORY_PROPERTY = "setup.plugins.directory";
    protected static final String SETUP_APPLICATION_ARTIFACT_PROPERTY = "setup.application.artifact";
    protected static final String SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY = "setup.documentations.directory";
    protected static final String SETUP_APPLICATION_NAME_PROPERTY = "setup.application.name";
    protected static final String SETUP_APPLICATION_VERSION_PROPERTY = "setup.application.version";
    protected static final String SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY = "setup.service.twitter.consumerKey";
    protected static final String SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY = "setup.service.twitter.consumerSecret";

    protected File pluginsDirectory;
    protected File applicationArtifact;
    protected File documentationsFolder;
    protected String applicationName;
    protected String applicationVersion;
    protected String twitterConsumerKey;
    protected String twitterConsumerSecret;

    protected SetupViewController controller;

    protected Parent getRootNode() throws IOException {
        final FXMLLoader loader = new FXMLLoader(SlideshowFXSetup.class.getResource("/com/twasyl/slideshowfx/setup/fxml/SetupView.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();

        final String license = IOUtils.read(SlideshowFXSetup.class.getResourceAsStream("/com/twasyl/slideshowfx/setup/license/LICENSE"));

        controller.addStep(new WelcomeStep(this.applicationName, this.applicationVersion))
                .addStep(new LicenseStep(license))
                .addStep(new InstallationLocationStep(this.applicationName, this.applicationVersion, this.applicationArtifact, this.documentationsFolder, this.twitterConsumerKey, this.twitterConsumerSecret))
                .addStep(new PluginsStep(this.pluginsDirectory))
                .addStep(new FinishStep(this.applicationName, this.applicationVersion));

        return root;
    }

    /**
     * Loads the properties used during the setup as the version, the various locations for plugins and documentation
     * and so on.
     * @return A never {@code null} {@link Properties} instance.
     * @throws IOException
     */
    protected Properties getSetupProperties() throws IOException {
        final Properties properties = new Properties();
        try (final InputStream input = getClass().getResourceAsStream("/com/twasyl/slideshowfx/setup/setup.properties")) {
            properties.load(input);
        }
        return properties;
    }

    @Override
    public void init() throws Exception {
        super.init();

        final Properties properties = getSetupProperties();

        this.pluginsDirectory = new File(properties.getProperty(SETUP_PLUGINS_DIRECTORY_PROPERTY));
        this.applicationArtifact = new File(properties.getProperty(SETUP_APPLICATION_ARTIFACT_PROPERTY));
        this.documentationsFolder = new File(properties.getProperty(SETUP_DOCUMENTATIONS_DIRECTORY_PROPERTY));
        this.applicationName = properties.getProperty(SETUP_APPLICATION_NAME_PROPERTY);
        this.applicationVersion = properties.getProperty(SETUP_APPLICATION_VERSION_PROPERTY);
        this.twitterConsumerKey = properties.getProperty(SETUP_SERVICE_TWITTER_CONSUMER_KEY_PROPERTY);
        this.twitterConsumerSecret = properties.getProperty(SETUP_SERVICE_TWITTER_CONSUMER_SECRET_PROPERTY);
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
