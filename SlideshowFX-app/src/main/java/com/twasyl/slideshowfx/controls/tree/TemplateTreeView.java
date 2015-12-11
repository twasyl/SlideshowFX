/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls.tree;

import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the TreeView that is used to managed the content of a template archive.
 * Because this TreeView represents the current content of a template archive, methods like
 * {@link #appendContentToTreeView(java.io.File, javafx.scene.control.TreeItem)} or
 * {@link #deleteContentOfTreeView(javafx.scene.control.TreeItem)} also perform operations on the
 * filesystem.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class TemplateTreeView extends TreeView<File> {
    private final Logger LOGGER = Logger.getLogger(TemplateTreeView.class.getName());

    private final ObjectProperty<EventHandler<MouseEvent>> onItemClick = new SimpleObjectProperty<>();

    /**
     * This method is used when something is drag over the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragOverItem = dragEvent -> {
        if(dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
            this.cleanCssClassForDragEvent((Node) dragEvent.getSource());
            ((Node) dragEvent.getSource()).getStyleClass().add("validDragOver");
        }

        dragEvent.consume();
    };

    /**
     * This method is used when something is dropped on the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragDroppedItem = dragEvent -> {
        Dragboard board = dragEvent.getDragboard();
        boolean dragSuccess = false;

        if(board.hasFiles()) {
            board.getFiles()
                    .stream()
                    .forEach(file -> {
                        final TreeItem<File> item = dragEvent.getSource() == this ? this.getRoot() : ((TreeCell<File>) dragEvent.getSource()).getTreeItem();
                        this.appendContentToTreeView(file, item);
                    });

            dragSuccess = true;
        }

        this.cleanCssClassForDragEvent((Node) dragEvent.getSource());
        dragEvent.setDropCompleted(dragSuccess);
        dragEvent.consume();
    };

    /**
     * This method is used when the drag is done on the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragDoneItem = dragEvent -> {
        this.cleanCssClassForDragEvent((Node) dragEvent.getSource());
    };


    /**
     * This method is used when the drag exits the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragExitedItem = dragEvent -> {
        this.cleanCssClassForDragEvent((Node) dragEvent.getSource());
        ((Node) dragEvent.getSource()).getStyleClass().add("noDragActive");
    };

    private final ObjectProperty<TemplateEngine> engine = new SimpleObjectProperty<>();

    public TemplateTreeView() {
        super();
        this.init();
    }

    public TemplateTreeView(TreeItem<File> root) {
        super(root);
        this.init();
    }

    private final void init() {
        this.setOnDragDropped(this.onDragDroppedItem);
        this.setOnDragOver(this.onDragOverItem);
        this.setOnDragDone(this.onDragDoneItem);
        this.setOnDragExited(this.onDragExitedItem);

        this.setCellFactory((TreeView<File> p) -> {
            FileTreeCell cell = new FileTreeCell();

            if(this.getOnItemClick() != null) cell.setOnMouseClicked(this.getOnItemClick());

            return cell;
        });
    }

    /**
     * Get the event handler that is registered to newly created TreeItem in this TreeView.
     * @return Get the event handler that is registered to newly created TreeItem in this TreeView.
     */
    public ObjectProperty<EventHandler<MouseEvent>> onItemClickProperty() { return this.onItemClick; }

    /**
     * Get the event handler that is registered to newly created TreeItem in this TreeView.
     * @return Get the event handler that is registered to newly created TreeItem in this TreeView.
     */
    public EventHandler<MouseEvent> getOnItemClick() { return onItemClick.get(); }

    /**
     * Set the event handler that is registered to newly created TreeItem in this TreeView.
     * @param onItemClick the new event that will be added to newly created TreeItems in this TreeView.
     */
    public void setOnItemClick(EventHandler<MouseEvent> onItemClick) { this.onItemClick.set(onItemClick); }

    /**
     * This method adds the given file to the parent TreeItem. If the file is a directory,
     * all files included in the directory will be added to the TreeView for a TreeItem corresponding the the current given file.
     * This method also copy the given file to the temporary archive folder.
     *
     * @param file The content to add to the TreeView.
     * @param parent The item that is the parent of the content to add.
     */
    public void appendContentToTreeView(File file, TreeItem<File> parent) {
        File relativeToParent = new File(parent.getValue(), file.getName());

        final TreeItem<File> treeItem = new TreeItem<>(relativeToParent);

        try {
            if (file.isDirectory()) {
                Files.createDirectories(relativeToParent.toPath());

                Arrays.stream(file.listFiles())
                        .forEach(subFile -> this.appendContentToTreeView(subFile, treeItem));
            } else {
                Files.copy(file.toPath(), relativeToParent.toPath());
            }

            parent.setExpanded(true);
            parent.getChildren().add(treeItem);
        } catch(IOException e) {
            LOGGER.log(Level.SEVERE, "Can not copy content", e);
        }
    }

    /**
     * Deletes the given item of the TreeView. If the item is the root or the template configuration item
     * of the {@link #engineProperty()}, it won't be deleted.
     * Note that this method also removes the file contained in the given item from the file system.
     *
     * @param item The item to remove from this TreeView.
     * @throws java.lang.NullPointerException If the item is null.
     * @throws java.io.IOException If an error occured when trying to delete the file corresponding to the item.
     */
    public void deleteContentOfTreeView(TreeItem<File> item) throws NullPointerException, IOException {
        if(item == null) throw new NullPointerException("The item can not be null");

        if(isItemDeletionEnabled(item)) {
            if(item.getValue().exists()) {
                if (item.getValue().isFile()) {
                    Files.delete(item.getValue().toPath());
                } else {
                    Files.walkFileTree(item.getValue().toPath(), new DeleteFileVisitor());
                }
            }

            item.getParent().getChildren().remove(item);
        }
    }

    /**
     * This method renames the given item with the new name. This method also renames the File
     * attached to this item on the file system.
     * Note that if the item is the root or the configuration file for the {@link #engineProperty()}, nothing is performed.
     *
     * @param item The item to rename.
     * @param name The new name of this item and file.
     * @throws java.lang.NullPointerException If the item or the name is null.
     * @throws java.lang.IllegalArgumentException If the name is empty.
     * @throws java.io.IOException If an error occured while renaming the file associated to this item.
     */
    public void renameContentOfTreeView(TreeItem<File> item, String name) throws NullPointerException, IllegalArgumentException, IOException {
        if(item == null) throw new NullPointerException("The item can not be null");
        if(name == null) throw new NullPointerException("The new name can not be null");
        if(name.isEmpty()) throw new IllegalArgumentException("The new name can not be empty");

        if(isItemRenamingAllowed(item)) {
            final Path currentFile = item.getValue().toPath();
            final File newFile = new File(item.getValue().getParent(), name);

            Files.move(currentFile, currentFile.resolveSibling(name));
            item.setValue(newFile);
        }
    }

    /**
     * Tests if the given item is allowed to be renamed. The root and the configuration item can not be renamed.
     *
     * @param item The item to test the rename on.
     * @return true if the item can be renamed, false otherwise.
     */
    public boolean isItemRenamingAllowed(TreeItem<File> item) {
        boolean canRename = false;

        if(item != this.getRoot()) {
            final File configurationFile = new File(this.getEngine().getWorkingDirectory(), this.getEngine().getConfigurationFilename());

            canRename = !item.getValue().equals(configurationFile);
        }

        return canRename;
    }

    /**
     * Tests if the given item is allowed to be deleted. The root and the configuration item can not be deleted.
     *
     * @param item The item to test the deletion on.
     * @return true if the item can be deleted, false otherwise.
     */
    public boolean isItemDeletionEnabled(TreeItem<File> item) {
        boolean canDelete = false;

        if(item != this.getRoot()) {
            final File configurationFile = new File(this.getEngine().getWorkingDirectory(), this.getEngine().getConfigurationFilename());

            canDelete = !item.getValue().equals(configurationFile);
        }

        return canDelete;
    }

    private void cleanCssClassForDragEvent(Node node) {
        node.getStyleClass().remove("noDragActive");
        node.getStyleClass().remove("validDragOver");
        node.getStyleClass().remove("invalidDragOver");
    }

    public EventHandler<DragEvent> getOnDragOverItem() { return onDragOverItem; }

    public EventHandler<DragEvent> getOnDragDroppedItem() { return onDragDroppedItem; }

    public EventHandler<DragEvent> getOnDragDoneItem() { return onDragDoneItem; }

    public EventHandler<DragEvent> getOnDragExitedItem() { return onDragExitedItem; }

    /**
     * Get the property containing the template engine associated to this TreeView.
     * @return The property containing the engine.
     */
    public ObjectProperty<TemplateEngine> engineProperty() { return this.engine; }
    /**
     * Return the template engine associated to this TreeView.
     * @return the template engine associated to this TreeView.
     */
    public TemplateEngine getEngine() { return this.engineProperty().get(); }

    /**
     * Set the template engine associated to this TreeView.
     * @param engine the new engine associated to this TreeView
     */
    public void setEngine(TemplateEngine engine) { this.engineProperty().set(engine); }
}
