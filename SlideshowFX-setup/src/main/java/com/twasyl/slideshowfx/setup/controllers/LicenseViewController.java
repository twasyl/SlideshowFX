package com.twasyl.slideshowfx.setup.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the {LicenseViewView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class LicenseViewController implements Initializable {

    @FXML private TextArea licence;
    @FXML private RadioButton acceptAgreement;

    private final BooleanProperty agreementAccepted = new SimpleBooleanProperty();

    /**
     * Indicates if the license agreement has been accepted or not.
     * @return {@code true} if the license agreement has been accepted, {@code false} otherwise.
     */
    public boolean isAgreementAccepted() {
        return this.agreementAccepted.get();
    }

    /**
     * Indicates if the license agreement has been accepted or not.
     * @return The {@link BooleanProperty} indicating if the license agreement has been accepted.
     */
    public BooleanProperty agreementAcceptedProperty() {
        return this.agreementAccepted;
    }

    /**
     * Set the license agreement for this setup.
     * @param text The text corresponding to the license agreement to set.
     * @return This instance of the controller.
     */
    public LicenseViewController setLicence(final String text) {
        this.licence.setText(text);
        return this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.agreementAccepted.bind(this.acceptAgreement.selectedProperty());
    }
}
