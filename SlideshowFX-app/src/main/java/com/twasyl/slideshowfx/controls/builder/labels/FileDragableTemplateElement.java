package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.FileTemplateElement;

/**
 * @author Thierry Wasylczenko
 */
public class FileDragableTemplateElement extends DragableTemplateElementLabel {

    public FileDragableTemplateElement() {
        super();
        this.setTemplateElementClassName(FileTemplateElement.class.getName());
    }
}
