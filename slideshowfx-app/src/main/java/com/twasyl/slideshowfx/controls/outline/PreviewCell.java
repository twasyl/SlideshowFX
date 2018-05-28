package com.twasyl.slideshowfx.controls.outline;

/*
 * Implementation of a {@link ListCell} allowing drag'n'drop gestures.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class PreviewCell extends ListCell<ImageView> {
    private ObjectProperty<EventHandler<PresentationOutlineEvent>> onSlideDeletionRequested = new SimpleObjectProperty<>();

    public PreviewCell() {
        this.setOnDragDetected(event -> {
            if (getItem() != null && !isEmpty()) {
                final Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                content.putString("");
                dragboard.setContent(content);
            }
            event.consume();
        });

        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        this.setOnDragDropped(event -> {
            final Dragboard dragboard = event.getDragboard();
            dragboard.getString();

            final PreviewCell source = (PreviewCell) event.getGestureSource();
            final ImageView from = source.getItem();
            final ImageView to = this.getItem();

            getListView().getItems().remove(from);
            final int i = getListView().getItems().indexOf(to);
            getListView().getItems().add(i, from);

            getListView().fireEvent(new PresentationOutlineEvent(PresentationOutlineEvent.SLIDE_MOVED, (String) from.getUserData(), (String) to.getUserData()));
            event.setDropCompleted(true);
            event.consume();
        });
    }

    @Override
    protected void updateItem(ImageView item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            this.setGraphic(item);
        }
    }

    public void setOnSlideDeletionRequested(final EventHandler<PresentationOutlineEvent> event) {
        this.onSlideDeletionRequested.set(event);
    }
}
