package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.StringTemplateElement;

/**
 * @author Thierry Wasylczenko
 */
public class StringDragableTemplateElement extends DragableTemplateElementLabel {

    public StringDragableTemplateElement() {
        super();
        this.setTemplateElementClassName(StringTemplateElement.class.getName());
    }
}
