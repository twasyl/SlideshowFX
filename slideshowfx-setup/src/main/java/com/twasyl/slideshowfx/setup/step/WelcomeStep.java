package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.app.SetupProperties;
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
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class WelcomeStep extends AbstractSetupStep {
    private static final Logger LOGGER = Logger.getLogger(WelcomeStep.class.getName());

    /**
     * Create an instance of the step.
     *
     */
    public WelcomeStep() {
        this.title("Welcome to the installation of " + SetupProperties.getInstance().getApplicationName());

        final FXMLLoader loader = new FXMLLoader(WelcomeStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/WelcomeView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((WelcomeViewController) this.controller)
                    .setApplicationName(SetupProperties.getInstance().getApplicationName())
                    .setApplicationVersion(SetupProperties.getInstance().getApplicationVersion());

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
