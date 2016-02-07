package com.twasyl.slideshowfx.controls.builder.labels;

import com.twasyl.slideshowfx.controls.builder.elements.DirectoryTemplateElement;

/**
 * @author Thierry Wasylczenko
 */
public class DirectoryDragableTemplateElement extends DragableTemplateElementLabel {

    public DirectoryDragableTemplateElement() {
        super();
        this.setTemplateElementClassName(DirectoryTemplateElement.class.getName());
    }
}
