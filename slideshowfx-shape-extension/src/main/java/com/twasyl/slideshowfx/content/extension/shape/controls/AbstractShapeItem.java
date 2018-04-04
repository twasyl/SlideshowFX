package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.IShape;

/**
 * Basic abstract implementation of a {@link IShapeItem}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public abstract class AbstractShapeItem<T extends IShape> implements IShapeItem<T> {
    private String label;

    protected AbstractShapeItem(final String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return this.label;
    }
}
