package com.twasyl.slideshowfx.ui.controls;

import com.twasyl.slideshowfx.ui.controls.validators.IValidator;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.Font;

/**
 * A simple text area allowing to zoom in and out using the mouse wheel.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class ZoomTextArea extends TextArea {
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private ReadOnlyBooleanProperty valid = new SimpleBooleanProperty();

    private IValidator<String> validator;

    {
        this.registerZoomEvent();
        this.textProperty().addListener((value, oldText, newText) -> {
            Boolean validValue = null;
            if (this.validator != null) {
                validValue = this.isValid();
            }
            ((SimpleBooleanProperty) this.valid).setValue(validValue);
        });
    }

    public ZoomTextArea() {
    }

    public ZoomTextArea(String text) {
        super(text);
    }

    private void registerZoomEvent() {
        this.registerZoomEventByScroll();
    }

    private void registerZoomEventByScroll() {
        this.addEventHandler(ScrollEvent.SCROLL, event -> {
            if (event.isShortcutDown()) {
                changeFontSize(event.getDeltaY());
            }
        });
    }

    private void changeFontSize(final double factor) {
        final double delta = factor > 0 ? 1 : -1;
        final Font font = this.getFont();

        this.setFont(new Font(font.getName(), font.getSize() + delta));
    }

    public IValidator<String> getValidator() {
        return validator;
    }

    public void setValidator(IValidator<String> validator) {
        this.validator = validator;
    }

    public boolean isValid() {
        if (this.getValidator() == null) {
            throw new IllegalArgumentException("No validator defined for the control");
        }

        final boolean valid = this.validator.isValid(this.getText());
        if (!valid) {
            this.pseudoClassStateChanged(ERROR, true);
        }

        return valid;
    }

    public ReadOnlyBooleanProperty validProperty() {
        return this.valid;
    }
}
