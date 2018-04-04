package com.twasyl.slideshowfx.content.extension.alert.controllers;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtensionController;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import com.twasyl.slideshowfx.ui.controls.validators.Validators;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller used by the {@code AlertContentExtension.fxml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class AlertContentExtensionController extends AbstractContentExtensionController {

    @FXML
    private ExtendedTextField title;
    @FXML
    private ExtendedTextField text;
    @FXML
    private ToggleGroup typeGroup;
    @FXML
    private RadioButton infoRB;
    @FXML
    private RadioButton successRB;
    @FXML
    private RadioButton warningRB;
    @FXML
    private RadioButton errorRB;
    @FXML
    private CheckBox allowClickOutside;
    @FXML
    private CheckBox showCancelButton;
    @FXML
    private ExtendedTextField buttonText;

    /**
     * Return the title entered in the UI for this alert.
     *
     * @return The title of the alert.
     */
    public String getTitle() {
        return this.title.getText();
    }

    /**
     * Return the additional text entered in the UI for this alert.
     *
     * @return The text for this alert.
     */
    public String getText() {
        return this.text.getText();
    }

    /**
     * Return the type of the alert. Currently four values can be returned:
     * <ul>
     * <li>info</li>
     * <li>success</li>
     * <li>warning</li>
     * <li>error</li>
     * </ul>
     *
     * @return Return the type of this alert.
     */
    public String getType() {
        String type = null;

        if (this.typeGroup.getSelectedToggle() == this.infoRB) type = "info";
        else if (this.typeGroup.getSelectedToggle() == this.successRB) type = "success";
        else if (this.typeGroup.getSelectedToggle() == this.warningRB) type = "warning";
        else if (this.typeGroup.getSelectedToggle() == this.errorRB) type = "error";

        return type;
    }

    public boolean isClickOutsideAllowed() {
        return this.allowClickOutside.isSelected();
    }

    public boolean isCancelButtonVisible() {
        return this.showCancelButton.isSelected();
    }

    public String getButtonText() {
        return this.buttonText.getText();
    }

    @Override
    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.title.validProperty());

        return property;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.title.setValidator(Validators.isNotEmpty());
    }
}
