package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Triangle;

/**
 * Implementation of {@link IShapeItem} that allows to insert triangles in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class TriangleItem extends AbstractShapeItem<Triangle> {

    public TriangleItem() {
        super("Triangle");
    }

    @Override
    public Triangle getShape() {
        return new Triangle();
    }
}
