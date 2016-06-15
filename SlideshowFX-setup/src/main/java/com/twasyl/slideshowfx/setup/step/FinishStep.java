package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.FinishViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * Step displayed when the setup of the application is finished.
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class FinishStep extends AbstractSetupStep {

    /**
     * Create a new finish step.
     * @param appName The name of the application.
     * @param appVersion The version of the application.
     */
    public FinishStep(final String appName, final String appVersion) {
        this.title("Installation successful");

        final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/setup/fxml/FinishView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((FinishViewController) this.controller).setApplicationName(appName)
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
