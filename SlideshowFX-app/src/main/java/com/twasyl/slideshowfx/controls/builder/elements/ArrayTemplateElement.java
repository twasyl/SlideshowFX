package com.twasyl.slideshowfx.controls.builder.elements;

/**
 * The ArrayTemplateElement extends {@link com.twasyl.slideshowfx.controls.builder.elements.ListTemplateElement}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class ArrayTemplateElement extends ListTemplateElement {

    public ArrayTemplateElement(String name) {
        super(name);

        this.opening.setText("[");
        this.closing.setText("]");
    }
}
