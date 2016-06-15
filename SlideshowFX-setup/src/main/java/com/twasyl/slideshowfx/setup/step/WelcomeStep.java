package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.WelcomeViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * A step displaying a welcome screen.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class WelcomeStep extends AbstractSetupStep {

    /**
     * Create an instance of the step.
     * @param appName The name of the application.
     * @param appVersion The version of the application.
     */
    public WelcomeStep(final String appName, final String appVersion) {
        this.title("Welcome to the installation of " + appName);

        final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/setup/fxml/WelcomeView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((WelcomeViewController) this.controller).setApplicationName(appName)
                    .setApplicationVersion(appVersion);

            this.validProperty().set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() throws SetupStepException {
    }

    @Override
    public void rollback() throws SetupStepException {
    }
}
