package com.twasyl.slideshowfx.content.extension.shape.controls;

import javafx.scene.control.ListCell;

/**
 * An implementation of {@link ListCell} for {@link IShapeItem} objects.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class ShapeCell extends ListCell<IShapeItem> {

    @Override
    protected void updateItem(IShapeItem drawing, boolean empty) {
        super.updateItem(drawing, empty);

        if (drawing != null && !empty) {
            this.setText(drawing.getLabel());
        }
    }
}
