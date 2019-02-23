package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.WelcomeViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * A step displaying a welcome screen.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class WelcomeStep extends AbstractSetupStep {
    private static final Logger LOGGER = Logger.getLogger(WelcomeStep.class.getName());

    /**
     * Create an instance of the step.
     *
     * @param appName    The name of the application.
     * @param appVersion The version of the application.
     */
    public WelcomeStep(final String appName, final String appVersion) {
        this.title("Welcome to the installation of " + appName);

        final FXMLLoader loader = new FXMLLoader(WelcomeStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/WelcomeView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((WelcomeViewController) this.controller).setApplicationName(appName)
                    .setApplicationVersion(appVersion);

            this.validProperty().set(true);
        } catch (IOException e) {
            LOGGER.log(SEVERE, "Can not find FXML", e);
        }
    }

    @Override
    public void execute() throws SetupStepException {
        // This step doesn't perform any operation
    }

    @Override
    public void rollback() throws SetupStepException {
        // This step doesn't perform any operation
    }
}
