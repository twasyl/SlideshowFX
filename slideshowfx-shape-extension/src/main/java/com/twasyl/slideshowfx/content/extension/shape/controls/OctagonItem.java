package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Octagon;

/**
 * Implementation of {@link IShapeItem} that allows to insert octagons in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class OctagonItem extends AbstractShapeItem<Octagon> {
    public OctagonItem() {
        super("Octagon");
    }

    @Override
    public Octagon getShape() {
        return new Octagon();
    }
}
