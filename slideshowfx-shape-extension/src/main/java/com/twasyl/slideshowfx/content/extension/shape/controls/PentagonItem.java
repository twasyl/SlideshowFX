package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.Pentagon;

/**
 * Implementation of {@link IShapeItem} that allows to insert pentagons in a
 * slide.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class PentagonItem extends AbstractShapeItem<Pentagon> {
    public PentagonItem() {
        super("Pentagon");
    }

    @Override
    public Pentagon getShape() {
        return new Pentagon();
    }
}
