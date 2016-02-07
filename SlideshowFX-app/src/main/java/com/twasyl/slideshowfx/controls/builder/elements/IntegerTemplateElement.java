package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.IntegerStringConverter;

/**
 * The StringTemplateElement allows to enter an integer as value in a text field.
 * It implements {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class IntegerTemplateElement  extends AbstractTemplateElement<Integer> {

    public IntegerTemplateElement(String name) {
        super();

        this.name.set(name);

        final TextField field = new TextField();
        field.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCode().isLetterKey() || event.getCode().isWhitespaceKey()) {
                event.consume();
            }
        });

        field.textProperty().bindBidirectional(this.value, new IntegerStringConverter());

        this.appendContent(field);
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(String.format("%1$s", getValue()));

        return builder.toString();
    }
}
