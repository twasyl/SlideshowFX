package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Ellipse;

/**
 * Implementation of {@link IShapeItem} that allows to insert ellipses in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class EllipseItem extends AbstractShapeItem<Ellipse> {

    public EllipseItem() {
        super("Ellipse");
    }

    @Override
    public Ellipse getShape() {
        return new Ellipse();
    }
}
