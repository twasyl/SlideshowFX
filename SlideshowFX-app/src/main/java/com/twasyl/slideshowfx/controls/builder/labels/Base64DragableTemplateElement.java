package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.Base64TemplateElement;

/**
 * This class allows to drag a {@link com.twasyl.slideshowfx.controls.builder.elements.Base64TemplateElement}
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class Base64DragableTemplateElement extends DragableTemplateElementLabel {

    public Base64DragableTemplateElement() {
        super();
        this.setTemplateElementClassName(Base64TemplateElement.class.getName());
    }
}
