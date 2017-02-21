package com.twasyl.slideshowfx.controls.tree;

import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import com.twasyl.slideshowfx.ui.controls.validators.Validators;
import com.twasyl.slideshowfx.utils.DialogHelper;
import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the TreeView that is used to managed the content of a template archive.
 * Because this TreeView represents the current content of a template archive, methods like
 * {@link #appendContentToTreeView(File, TreeItem)} or
 * {@link #deleteContentOfTreeView(TreeItem)} also perform operations on the
 * filesystem.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class TemplateTreeView extends TreeView<File> {
    private final Logger LOGGER = Logger.getLogger(TemplateTreeView.class.getName());

    private static final PseudoClass VALID_DRAG = PseudoClass.getPseudoClass("validDrag");
    private static final PseudoClass INVALID_DRAG = PseudoClass.getPseudoClass("invalidDrag");

    private final ObjectProperty<EventHandler<MouseEvent>> onItemClick = new SimpleObjectProperty<>();

    /**
     * This method is used when something is drag over the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragOverItem = dragEvent -> {
        this.removeCustomPseudoClass((Node) dragEvent.getSource());

        if (dragEvent.getDragboard().hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
            ((Node) dragEvent.getSource()).pseudoClassStateChanged(VALID_DRAG, true);

            if (dragEvent.getSource() instanceof TreeCell) {
                final TreeCell cell = (TreeCell) dragEvent.getSource();
                if (cell != null) {
                    cell.getTreeItem().setExpanded(true);
                }
            }
        } else {
            ((Node) dragEvent.getSource()).pseudoClassStateChanged(INVALID_DRAG, true);
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

        if (board.hasFiles()) {
            board.getFiles()
                    .stream()
                    .forEach(file -> {
                        final TreeItem<File> item = dragEvent.getSource() == this ? this.getRoot() : ((TreeCell<File>) dragEvent.getSource()).getTreeItem();
                        this.appendContentToTreeView(file, item);
                    });

            dragSuccess = true;
        }

        this.removeCustomPseudoClass((Node) dragEvent.getSource());
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
        removeCustomPseudoClass((Node) dragEvent.getSource());
    };

    /**
     * This method is used when the drag exits the TreeView that shows the content of the template's archive.
     * Only files are accepted.
     *
     * @param dragEvent The event associated to the drag
     */
    private EventHandler<DragEvent> onDragExitedItem = dragEvent -> {
        removeCustomPseudoClass((Node) dragEvent.getSource());
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
        this.getStyleClass().add("template-tree-view");
        this.setOnDragDropped(this.onDragDroppedItem);
        this.setOnDragEntered(this.onDragOverItem);
        this.setOnDragDone(this.onDragDoneItem);
        this.setOnDragExited(this.onDragExitedItem);

        this.setCellFactory((TreeView<File> p) -> {
            FileTreeCell cell = new FileTreeCell();

            if (this.getOnItemClick() != null) cell.setOnMouseClicked(this.getOnItemClick());

            return cell;
        });
    }

    /**
     * Get the event handler that is registered to newly created TreeItem in this TreeView.
     *
     * @return Get the event handler that is registered to newly created TreeItem in this TreeView.
     */
    public ObjectProperty<EventHandler<MouseEvent>> onItemClickProperty() {
        return this.onItemClick;
    }

    /**
     * Get the event handler that is registered to newly created TreeItem in this TreeView.
     *
     * @return Get the event handler that is registered to newly created TreeItem in this TreeView.
     */
    public EventHandler<MouseEvent> getOnItemClick() {
        return onItemClick.get();
    }

    /**
     * Set the event handler that is registered to newly created TreeItem in this TreeView.
     *
     * @param onItemClick the new event that will be added to newly created TreeItems in this TreeView.
     */
    public void setOnItemClick(EventHandler<MouseEvent> onItemClick) {
        this.onItemClick.set(onItemClick);
    }

    /**
     * This method adds the given file to the selected item in the tree view. If there is no selection, the root of the
     * tree view will be used.
     * If the file is a directory, all files included in the directory will be added to the tree view for a TreeItem
     * corresponding the the current given file.
     * This method also copy the given file to the temporary archive folder.
     *
     * @param file The content to add to the TreeView.
     * @return Return the {@link TreeItem} that has been created.
     */
    public TreeItem<File> appendContentToTreeView(File file) {
        final TreeItem<File> parent = this.getParentDirectoryOfSelection();

        return this.appendContentToTreeView(file, parent);
    }

    /**
     * This method adds the given file to the parent {@link TreeItem}. If the file is a directory,
     * all files included in the directory will be added to the TreeView for a TreeItem corresponding the the current given file.
     * This method also copy the given file to the temporary archive folder.
     *
     * @param file   The content to add to the TreeView.
     * @param parent The item that is the parent of the content to add.
     * @return Return the {@link TreeItem} that has been created.
     */
    public TreeItem<File> appendContentToTreeView(File file, TreeItem<File> parent) {
        File relativeToParent = new File(parent.getValue(), file.getName());

        TreeItem<File> treeItem = new TreeItem<>(relativeToParent);

        try {
            if (file.isDirectory()) {
                Files.createDirectories(relativeToParent.toPath());

                for (final File child : file.listFiles()) {
                    this.appendContentToTreeView(child, treeItem);
                }
            } else {
                Files.copy(file.toPath(), relativeToParent.toPath());
            }

            parent.getChildren().add(treeItem);
            this.getSelectionModel().select(treeItem);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not copy content", e);
            treeItem = null;
        }
        return treeItem;
    }

    /**
     * This methods will prompt the user a name of a file and create an empty file under the current selection.
     */
    public void promptUserAndCreateNewFile() {
        final ExtendedTextField fileName = new ExtendedTextField("File name", true);
        fileName.setValidator(Validators.isNotEmpty());

        final HBox pane = new HBox(5, fileName);

        final ButtonType answer = DialogHelper.showCancellableDialog("Add a new file", pane);

        if (answer == ButtonType.OK && fileName.isValid()) {
            this.createFileUnderSelection(fileName.getText());
        }
    }

    /**
     * This methods will prompt the user a name of a file and create an empty file under the current selection.
     */
    public void promptUserAndCreateNewDirectory() {
        final ExtendedTextField directoryName = new ExtendedTextField("Directory name", true);
        directoryName.setValidator(Validators.isNotEmpty());

        final HBox pane = new HBox(5, directoryName);

        final ButtonType answer = DialogHelper.showCancellableDialog("Add a new directory", pane);

        if (answer == ButtonType.OK && directoryName.isValid()) {
            this.createDirectoryUnderSelection(directoryName.getText());
        }
    }

    /**
     * Creates an empty file named according the given {@code fileName}. The file is created under the current selection.
     * If the current selection is a directory, an empty file will be created inside this directory.
     * If the current selection is a file, the first parent will be determined and the file will be created into this
     * parent.
     * If there is no selection, then the file will be created under the root.
     *
     * @param fileName The name of the file that must be created.
     */
    public void createFileUnderSelection(final String fileName) {
        if (fileName != null && !fileName.trim().isEmpty()) {
            final TreeItem<File> parent = getParentDirectoryOfSelection();

            if (parent != null) {
                final File newFile = new File(parent.getValue(), fileName.trim());

                try {
                    Files.createFile(newFile.toPath());
                    final TreeItem<File> newFileItem = new TreeItem<>(newFile);

                    parent.getChildren().add(newFileItem);
                    this.getSelectionModel().select(newFileItem);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not create the empty file", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Can not determine where the file must be created");
            }
        }
    }

    /**
     * Creates an empty directory named according the given {@code directoryName}. The file is created under the
     * current selection.
     * If the current selection is a directory, an empty directory will be created inside this directory.
     * If the current selection is a file, the first parent will be determined and the directory will be created into
     * this parent.
     * If there is no selection, then the directory will be created under the root.
     * <p>
     * The directory's name can be a path where each directory to create is separated by a {@code /}. For instance:
     * {@code dir/subdir} will create a subdir directory in a dir directory, itself created under the selection.
     *
     * @param directoryName The name of the directory that must be created.
     */
    public void createDirectoryUnderSelection(final String directoryName) {
        final TreeItem<File> parent = getParentDirectoryOfSelection();

        if (directoryName != null && !directoryName.trim().isEmpty() && parent != null) {
            final String[] directories = directoryName.trim().split("/");

            TreeItem<File> createdDirectory = parent;
            int index = 0;

            do {
                createdDirectory = this.createDirectoryUnderParent(createdDirectory, directories[index++]);
            } while (createdDirectory != null && index < directories.length);
        }
    }

    /**
     * Creates an empty directory under the given parent. The parent must not be {@code null} and it's value must be
     * non {@code null} and be a directory.
     * The name of the directory must be non {@code null} and not empty.
     *
     * @param parent        The parent of the directory to create.
     * @param directoryName The name of the directory to create.
     * @return Return the created directory.
     */
    private TreeItem<File> createDirectoryUnderParent(final TreeItem<File> parent, final String directoryName) {
        TreeItem<File> newDirectoryItem = null;

        if (directoryName != null && !directoryName.trim().isEmpty()) {
            if (parent != null && parent.getValue() != null) {
                if (parent.getValue().isDirectory()) {
                    final File newDirectory = new File(parent.getValue(), directoryName.trim());

                    if (!newDirectory.exists()) {
                        try {
                            Files.createDirectory(newDirectory.toPath());
                            newDirectoryItem = new TreeItem<>(newDirectory);

                            parent.getChildren().add(newDirectoryItem);
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Can not create the empty file", e);
                        }
                    } else {
                        newDirectoryItem = parent.getChildren()
                                .stream()
                                .filter(item -> item.getValue().equals(newDirectory))
                                .findFirst()
                                .orElse(null);
                    }

                    if (newDirectoryItem != null) {
                        this.getSelectionModel().select(newDirectoryItem);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Can not create a directory because the parent is not a directory");
                }
            } else {
                LOGGER.log(Level.WARNING, "Can not determine where the file must be created");
            }
        }

        return newDirectoryItem;
    }

    /**
     * Determine the parent of the current selection that is a directory. If the selection is a directory itself,
     * then it is returned. If the selection is a file, then it's parent is returned. If there is no selection, the
     * root of this {@link TemplateTreeView} is returned.
     *
     * @return The parent of the selection that is a directory.
     */
    protected TreeItem<File> getParentDirectoryOfSelection() {
        final TreeItem<File> selection = this.getSelectionModel().getSelectedItem();
        final TreeItem<File> parent;

        if (selection == null) {
            parent = getRoot();
        } else if (selection.getValue() != null && selection.getValue().isDirectory()) {
            parent = selection;
        } else if (selection.getValue() != null && selection.getValue().isFile()) {
            parent = selection.getParent();
        } else {
            parent = null;
        }

        return parent;
    }

    /**
     * Closes recursively the given {@link TreeItem}.
     *
     * @param item The item to close recusively.
     */
    public void closeItem(final TreeItem<File> item) {
        if (item != null) {
            item.setExpanded(false);

            if(!item.isLeaf()) {
                item.getChildren().forEach(this::closeItem);
            }
        }
    }

    /**
     * Deletes the given item of the TreeView. If the item is the root or the template configuration item
     * of the {@link #engineProperty()}, it won't be deleted.
     * Note that this method also removes the file contained in the given item from the file system.
     *
     * @param item The item to remove from this TreeView.
     * @throws NullPointerException If the item is null.
     * @throws IOException          If an error occured when trying to delete the file corresponding to the item.
     */
    public void deleteContentOfTreeView(TreeItem<File> item) throws NullPointerException, IOException {
        if (item == null) throw new NullPointerException("The item can not be null");

        if (isItemDeletionEnabled(item)) {
            if (item.getValue().exists()) {
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
     * @throws NullPointerException     If the item or the name is null.
     * @throws IllegalArgumentException If the name is empty.
     * @throws IOException              If an error occured while renaming the file associated to this item.
     */
    public void renameContentOfTreeView(TreeItem<File> item, String name) throws NullPointerException, IllegalArgumentException, IOException {
        if (item == null) throw new NullPointerException("The item can not be null");
        if (name == null) throw new NullPointerException("The new name can not be null");
        if (name.isEmpty()) throw new IllegalArgumentException("The new name can not be empty");

        if (isItemRenamingAllowed(item)) {
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

        if (item != this.getRoot()) {
            final File configurationFile = new File(this.getEngine().getWorkingDirectory(), this.getEngine().getConfigurationFilename());

            canRename = item != null && item.getValue() != null && !item.getValue().equals(configurationFile);
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

        if (item != null && item != this.getRoot()) {
            final File configurationFile = new File(this.getEngine().getWorkingDirectory(), this.getEngine().getConfigurationFilename());

            canDelete = !item.getValue().equals(configurationFile);
        }

        return canDelete;
    }

    private void removeCustomPseudoClass(Node node) {
        node.pseudoClassStateChanged(VALID_DRAG, false);
        node.pseudoClassStateChanged(INVALID_DRAG, false);
    }

    public EventHandler<DragEvent> getOnDragOverItem() {
        return onDragOverItem;
    }

    public EventHandler<DragEvent> getOnDragDroppedItem() {
        return onDragDroppedItem;
    }

    public EventHandler<DragEvent> getOnDragDoneItem() {
        return onDragDoneItem;
    }

    public EventHandler<DragEvent> getOnDragExitedItem() {
        return onDragExitedItem;
    }

    /**
     * Get the property containing the template engine associated to this TreeView.
     *
     * @return The property containing the engine.
     */
    public ObjectProperty<TemplateEngine> engineProperty() {
        return this.engine;
    }

    /**
     * Return the template engine associated to this TreeView.
     *
     * @return the template engine associated to this TreeView.
     */
    public TemplateEngine getEngine() {
        return this.engineProperty().get();
    }

    /**
     * Set the template engine associated to this TreeView.
     *
     * @param engine the new engine associated to this TreeView
     */
    public void setEngine(TemplateEngine engine) {
        this.engineProperty().set(engine);
    }
}
