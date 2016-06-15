package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.setup.controllers.LicenseViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * A step allowing the acknowledge a license agreement.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class LicenseStep extends AbstractSetupStep {

    /**
     * Create an instance of the step.
     * @param licence The text of the license agreement.
     */
    public LicenseStep(final String licence) {
        this.title("License");

        final FXMLLoader loader = new FXMLLoader(ResourceHelper.getURL("/com/twasyl/slideshowfx/setup/fxml/LicenseView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((LicenseViewController) this.controller).setLicence(licence);

            this.validProperty().bind(((LicenseViewController) this.controller).agreementAcceptedProperty());
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
