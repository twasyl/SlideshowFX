/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.builder.elements;

import javafx.scene.control.TextField;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * The Base64TemplateElement allows to enter a String as value in a text field. The value sent to
 * this element must be a Base64 encoded String and will be decoded for display and encoded when calling
 * {@link #getAsString()}.
 * It implements {@link com.twasyl.slideshowfx.controls.builder.elements.AbstractTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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
        super.setValue(new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8));
    }

    @Override
    public String getAsString() {
        final StringBuilder builder = new StringBuilder();

        if(getName() != null) builder.append(String.format("\"%1$s\": ", getName()));

        builder.append(String.format("\"%1$s\"", Base64.getEncoder().encodeToString(getValue().getBytes(StandardCharsets.UTF_8))));

        return builder.toString();
    }
}
