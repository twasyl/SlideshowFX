package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Circle;

/**
 * Implementation of {@link IShapeItem} that allows to insert circles in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class CircleItem extends AbstractShapeItem<Circle> {

    public CircleItem() {
        super("Circle");
    }

    @Override
    public Circle getShape() {
        return new Circle();
    }
}
