package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.LicenseViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * A step allowing the acknowledge a license agreement.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class LicenseStep extends AbstractSetupStep<LicenseViewController> {
    private static final Logger LOGGER = Logger.getLogger(LicenseStep.class.getName());

    /**
     * Create an instance of the step.
     *
     * @param licence The text of the license agreement.
     */
    public LicenseStep(final String licence) {
        this.title("License");

        final FXMLLoader loader = new FXMLLoader(LicenseStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/LicenseView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            this.controller.setLicence(licence);

            this.validProperty().bind(this.controller.agreementAcceptedProperty());
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
