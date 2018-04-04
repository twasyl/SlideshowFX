package com.twasyl.slideshowfx.controls.builder.nodes;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.icons.Icon;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import static com.twasyl.slideshowfx.ui.controls.validators.Validators.isNotEmpty;

/**
 * Control allowing to define a default template variable.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.3
 */
public class TemplateVariable extends FlowPane {
    private ExtendedTextField name = new ExtendedTextField("Name", true);
    private ExtendedTextField value = new ExtendedTextField("Value", true);
    private Button delete = new Button();

    public TemplateVariable() {
        super(5, 5);

        this.initializeMandatoryFields();
        this.initializeDeleteButton();

        final HBox group1 = new HBox(5, value, delete);
        group1.setAlignment(Pos.BOTTOM_LEFT);

        this.getChildren().addAll(name, group1);
    }

    private void initializeMandatoryFields() {
        this.name.setValidator(isNotEmpty());
        this.value.setValidator(isNotEmpty());
    }

    private void initializeDeleteButton() {
        this.delete.getStyleClass().add("delete-default-template-variable");
        this.delete.setGraphic(new FontAwesome(Icon.TRASH_ALT));
        this.delete.setTooltip(new Tooltip("Delete this default template variable"));
    }

    public void setOnDelete(final EventHandler<ActionEvent> action) {
        this.delete.setOnAction(action);
    }

    public String getName() {
        return this.name.getText();
    }

    public void setName(final String name) {
        this.name.setText(name);
    }

    public String getValue() {
        return this.value.getText();
    }

    public void setValue(final String value) {
        this.value.setText(value);
    }

    /**
     * Check if this template variable is valis. The variable is considered valid if it's name and value are valid.
     *
     * @return {@code true} if the variable is valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return this.isNameValid() && this.isValueValid();
    }

    public boolean isNameValid() {
        return this.name.isValid();
    }

    public boolean isValueValid() {
        return this.value.isValid();
    }
}
