package com.twasyl.slideshowfx.controls.outline;

/*
 * Implementation of a {@link ListCell} allowing drag'n'drop gestures.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */

import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import static com.twasyl.slideshowfx.controls.outline.PresentationOutlineEvent.SLIDE_DELETION_REQUESTED;

public class PreviewCell extends ListCell<ImageView> {

    public PreviewCell() {
        this.initializeDragDropBehaviour();
        this.initializeContextMenu();
        this.setPadding(Insets.EMPTY);
    }

    /**
     * This method is responsible for initializing the drag'n'drop behaviour of this cell.
     */
    private void initializeDragDropBehaviour() {
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

    private void initializeContextMenu() {
        final MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> {
            if (getItem() != null) {
                final String sourceSlideId = (String) getItem().getUserData();
                final PresentationOutlineEvent requestSlideDeletion = new PresentationOutlineEvent(SLIDE_DELETION_REQUESTED, sourceSlideId, null);
                getListView().fireEvent(requestSlideDeletion);
            }
        });

        final ContextMenu menu = new ContextMenu(delete);
        setContextMenu(menu);
    }

    @Override
    protected void updateItem(ImageView item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null && !empty) {
            this.setGraphic(item);
        } else {
            this.setGraphic(null);
        }
    }
}
