package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.scene.control.TextField;

import java.util.Base64;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * The Base64TemplateElement allows to enter a String as value in a text field. The value sent to
 * this element must be a Base64 encoded String and will be decoded for display and encoded when calling
 * {@link #getAsString()}.
 * It implements {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class Base64TemplateElement extends AbstractTemplateElement<String> {
    private static final Logger LOGGER = Logger.getLogger(Base64TemplateElement.class.getName());

    public Base64TemplateElement(String name) {
        super();

        this.name.set(name);

        final TextField field = new TextField();
        field.textProperty().bindBidirectional(this.value);

        this.appendContent(field);
    }

    @Override
    public void setValue(String value) {
        super.setValue(new String(Base64.getDecoder().decode(value), getDefaultCharset()));
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(String.format("\"%1$s\"", Base64.getEncoder().encodeToString(getValue().getBytes(getDefaultCharset()))));

        return builder.toString();
    }
}
