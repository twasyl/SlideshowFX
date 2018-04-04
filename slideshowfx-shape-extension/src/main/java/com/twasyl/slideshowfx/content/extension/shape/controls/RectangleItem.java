package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Rectangle;

/**
 * Implementation of {@link IShapeItem} that allows to insert rectangles in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class RectangleItem extends AbstractShapeItem<Rectangle> {

    public RectangleItem() {
        super("Rectangle");
    }

    @Override
    public Rectangle getShape() {
        return new Rectangle();
    }
}
