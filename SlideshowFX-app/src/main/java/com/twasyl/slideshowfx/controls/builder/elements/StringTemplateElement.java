package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.scene.control.TextField;

/**
 * The StringTemplateElement allows to enter a String as value in a text field.
 * It implements {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class StringTemplateElement extends AbstractTemplateElement<String> {

    public StringTemplateElement(String name) {
        super();

        this.name.set(name);

        final TextField field = new TextField();
        field.textProperty().bindBidirectional(this.value);

        this.appendContent(field);
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(String.format("\"%1$s\"", getValue()));

        return builder.toString();
    }
}
