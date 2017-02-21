package com.twasyl.slideshowfx.controls.tree;

import com.twasyl.slideshowfx.utils.DialogHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to create TreeCell for a TreeView containing files.
 * It defines a actions possible when the {@link ContextMenu} is triggered for this cell.
 * It also set the drag events defined in the parent {@link TemplateTreeView} to this
 * cell.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class FileTreeCell extends TreeCell<File> {
    private static final Logger LOGGER = Logger.getLogger(FileTreeCell.class.getName());

    public FileTreeCell() {
        super();
    }

    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);

        initializeDragEvents(item);
        initializeGraphic();
        defineCellText();
        defineContextMenu();
    }

    /**
     * Initialize drag events to the current {@link FileTreeCell}. Drag events are only defined if the given item is a
     * a {@link File#isDirectory() directory}. If the item is not a directory, then this method ensures that all
     * drag events are removed from this cell.
     *
     * @param item The file for which drag events will be eventually set.
     */
    protected void initializeDragEvents(final File item) {
        if (item != null && item.isDirectory()) {
            setOnDragOver(((TemplateTreeView) getTreeView()).getOnDragOverItem());
            setOnDragDropped(((TemplateTreeView) getTreeView()).getOnDragDroppedItem());
            setOnDragDone(((TemplateTreeView) getTreeView()).getOnDragDoneItem());
            setOnDragExited(((TemplateTreeView) getTreeView()).getOnDragExitedItem());
        } else {
            setOnDragOver(null);
            setOnDragDropped(null);
            setOnDragDone(null);
            setOnDragExited(null);
        }
    }

    /**
     * Defines the {@link #setGraphic(Node) graphic} of this cell according the availability of the value and the type
     * of file this cell is hosting: a file or a directory.
     */
    protected void initializeGraphic() {
        if (this.getTreeItem() != null && this.getTreeItem().getValue() != null) {

            if(this.getTreeItem().getValue().isDirectory()) {
                if (this.getTreeItem().isExpanded()) {
                    setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN));
                } else {
                    setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FOLDER));
                }
            } else {
                setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT_ALT));
            }
        } else {
            setGraphic(null);
        }
    }

    /**
     * Define the {@link #setText(String) text} of this cell. If the {@link TreeView#getRoot() root} of the {@link TreeView}
     * hosting this cell is equal to the current {@link #getTreeItem() item}, then {@code /} is defined as text,
     * otherwise it is the {@link File#getName() name} of the provided file.
     */
    protected void defineCellText() {
        if (isEmpty()) {
            setText("");
        } else if (getTreeItem() == null) {
            setText("null");
        } else if (getTreeView().getRoot() == getTreeItem()) {
            setText("/");
        } else {
            setText(getTreeItem().getValue().getName());
        }
    }

    /**
     * Define the {@link ContextMenu} of this cell. This method can be called each time the item of the cell changes.
     */
    protected void defineContextMenu() {
        if (getContextMenu() == null) {
            this.setContextMenu(new ContextMenu());
        }

        getContextMenu().getItems().clear();

        final TemplateTreeView treeView = (TemplateTreeView) getTreeView();

        if (getItem() != null && getItem().isDirectory()) {
            getContextMenu().getItems().add(this.createNewFileMenuItem());
            getContextMenu().getItems().add(this.createNewDirectoryMenuItem());
        }

        if (treeView.isItemRenamingAllowed(getTreeItem())) {
            getContextMenu().getItems().add(this.createRenameMenuItem());
        }

        if (treeView.isItemDeletionEnabled(getTreeItem())) {
            getContextMenu().getItems().add(this.createDeleteMenuItem());
        }
    }

    /**
     * Creates the {@link MenuItem} that will allow to create a new file under the directory this cell is hosting.
     *
     * @return The {@link MenuItem} allowing to create a new file.
     */
    protected MenuItem createNewFileMenuItem() {
        final MenuItem newFile = new MenuItem("New file");
        newFile.setOnAction(event -> {
            final TemplateTreeView treeView = (TemplateTreeView) getTreeView();
            treeView.promptUserAndCreateNewFile();
        });
        return newFile;
    }

    /**
     * Creates the {@link MenuItem} that will allow to create a new directory under the directory this cell is hosting.
     *
     * @return The {@link MenuItem} allowing to create a new directory.
     */
    protected MenuItem createNewDirectoryMenuItem() {
        final MenuItem newFile = new MenuItem("New directory");
        newFile.setOnAction(event -> {
            final TemplateTreeView treeView = (TemplateTreeView) getTreeView();
            treeView.promptUserAndCreateNewDirectory();
        });
        return newFile;
    }

    /**
     * Creates the {@link MenuItem} that will allow to rename the file or directory this cell is hosting.
     *
     * @return The {@link MenuItem} allowing to rename contents.
     */
    protected MenuItem createRenameMenuItem() {
        final MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(event -> {
            try {
                final TextField textField = new TextField();
                textField.setText(this.getTreeItem().getValue() == null ? "" : this.getTreeItem().getValue().getName());

                final Label label = new Label("New name: ");
                label.setLabelFor(textField);

                final HBox hbox = new HBox(5);
                hbox.getChildren().addAll(label, textField);

                final ButtonType response = DialogHelper.showCancellableDialog("Rename", hbox);
                if (response != null && response == ButtonType.OK) {
                    ((TemplateTreeView) this.getTreeView()).renameContentOfTreeView(this.getTreeItem(), textField.getText());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not rename item", e);
            }
        });
        return renameItem;
    }

    /**
     * Creates the {@link MenuItem} that will allow to delete the file or directory this cell is hosting.
     *
     * @return The {@link MenuItem} allowing to delete contents.
     */
    protected MenuItem createDeleteMenuItem() {
        final MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            try {
                final ButtonType answer = DialogHelper.showConfirmationAlert("Delete", "Are you sure you want to delete " + this.getItem().getName() + "?");
                if (answer == ButtonType.YES) {
                    ((TemplateTreeView) this.getTreeView()).deleteContentOfTreeView(this.getTreeItem());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not delete item", e);
            }
        });
        return deleteItem;
    }
}
