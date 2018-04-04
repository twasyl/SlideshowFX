package com.twasyl.slideshowfx.content.extension.shape.controls;

import com.twasyl.slideshowfx.content.extension.shape.beans.IShape;

/**
 * An item to be displayed in a {@link javafx.scene.control.ListView}.
 * An item has a label and can create a given type of {@link IShape}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public interface IShapeItem<T extends IShape> {

    /**
     * The label of the item displayed in a {@link javafx.scene.control.ListView}.
     *
     * @return The label of the item.
     */
    String getLabel();

    /**
     * Creates an instance of {@link IShape} that must be inserted inside a slide.
     * This method should always return a new instance of {@link IShape}.
     *
     * @return The drawing to insert.
     */
    T getShape();
}
