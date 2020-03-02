package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.controls.builder.editor.*;
import com.twasyl.slideshowfx.controls.tree.FileTreeCell;
import com.twasyl.slideshowfx.controls.tree.TemplateTreeView;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.engine.template.TemplateEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION;
import static com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter.TEMPLATE_FILTER;
import static java.util.logging.Level.SEVERE;

/**
 * Controller class used for the Template Builder.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class TemplateBuilderController implements ThemeAwareController {
    private static final Logger LOGGER = Logger.getLogger(TemplateBuilderController.class.getName());

    @FXML
    private BorderPane templateBuilder;
    @FXML
    private TemplateTreeView templateContentTreeView;
    @FXML
    private TabPane openedFiles;

    private Stage stage;
    private TemplateEngine templateEngine;

    /**
     * Get the stage where this TemplateBuilder is in.
     *
     * @return The stage where this TemplateBuilder is in.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Set the stage where this TemplateBuilder will be in.
     *
     * @param stage The new stage where the TemplateBuilder will be in.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * This method opens a dialog box only allowing the selection of directories.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void addFolderToTreeView(ActionEvent event) {
        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Add content");
        final File directory = chooser.showDialog(null);

        if (directory != null) {
            this.templateContentTreeView.appendContentToTreeView(directory);
        }
    }

    /**
     * This method opens a dialog box only allowing the selection of files.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void addFileToTreeView(ActionEvent event) {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle("Add content");
        final File file = chooser.showOpenDialog(null);

        if (file != null) {
            this.templateContentTreeView.appendContentToTreeView(file);
        }
    }

    /**
     * Build the current template archive. This method checks if the archive has already been saved
     * or not. If so, the archive will be overwritten otherwise a dialog asks the user where to save it.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void buildTemplateArchive(ActionEvent event) {
        File destination = this.templateEngine.getArchive();

        if (destination == null) {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(TEMPLATE_FILTER);
            destination = chooser.showSaveDialog(null);
        }

        if (destination != null) {
            // Manage if the file name doesn't end with the template extension.
            if (!destination.getName().endsWith(DEFAULT_DOTTED_ARCHIVE_EXTENSION)) {
                destination = new File(destination.getAbsolutePath().concat(DEFAULT_DOTTED_ARCHIVE_EXTENSION));
            }

            this.templateEngine.setArchive(destination);
            try {
                this.templateEngine.saveArchive();
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Can not save the template", e);
            }
        }
    }

    /**
     * Build the current template archive.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void buildAsTemplateArchive(ActionEvent event) {

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(TEMPLATE_FILTER);
        File destination = chooser.showSaveDialog(null);

        if (destination != null) {
            this.templateEngine.setArchive(destination);
            try {
                this.templateEngine.saveArchive();
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Can not save the template", e);
            }
        }
    }

    /**
     * Save the current opened file.
     *
     * @param event The event associated to the click
     */
    @FXML
    private void saveCurrentFile(ActionEvent event) {
        IFileEditor currentFile = (IFileEditor) this.openedFiles.getSelectionModel().getSelectedItem();

        if (currentFile != null) {
            currentFile.saveContent();
        }
    }

    /**
     * Save all opened files.
     *
     * @param event The event associated to the click
     */
    @FXML
    private void saveAllFiles(ActionEvent event) {
        this.openedFiles.getTabs()
                .stream()
                .filter(tab -> tab instanceof IFileEditor)
                .map(tab -> (IFileEditor) tab)
                .forEach(IFileEditor::saveContent);
    }

    /**
     * Delete the selection from the TreeView and the filesystem.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void deleteFromTreeView(ActionEvent event) {
        ObservableList<TreeItem<File>> selectedItems = this.templateContentTreeView.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            final ButtonType answer = DialogHelper.showConfirmationAlert("Delete selection", "Are you sure you want to delete the selection?");

            if (answer == ButtonType.YES) {
                selectedItems.filtered(item -> item != this.templateContentTreeView.getRoot())
                        .forEach(item -> {
                            try {
                                this.templateContentTreeView.deleteContentOfTreeView(item);
                            } catch (IOException e) {
                                DialogHelper.showError("Error", "Can not delete the content");
                            }
                        });
            }
        }
    }

    /**
     * Allow the user to create a directory in the template. This method asks the user for the given directory name
     * (a value with slashes will create multiple directory) and creates the desired directory in the selection of the
     * TreeView. If there is no selection, the directory will be created at the root of this template.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void createDirectory(ActionEvent event) {
        this.templateContentTreeView.promptUserAndCreateNewDirectory();
    }

    /**
     * Allow the user to create am empty file in the template. This method asks the user for the given file name
     * and creates the desired file in the selection of the TreeView. If there is no selection, the file will be created
     * at the root of this template.
     *
     * @param event The event associated to button clicked to call this method.
     */
    @FXML
    private void createFile(ActionEvent event) {
        this.templateContentTreeView.promptUserAndCreateNewFile();
    }

    /**
     * Get the template engine used for the builder.
     *
     * @return The templated builder used for the builder.
     */
    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Set the new template engine for this builder. The engine must be fully initialized before calling this method
     * because it is used to initialize the view.
     *
     * @param templateEngine The template engine to be used.
     */
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;

        if (this.templateEngine != null) {

            if (this.templateEngine.getWorkingDirectory() == null) {
                this.templateEngine.setWorkingDirectory(this.templateEngine.generateWorkingDirectory());
            }

            final TreeItem root = new TreeItem(this.templateEngine.getWorkingDirectory());
            root.setExpanded(true);

            this.templateContentTreeView.setEngine(this.templateEngine);
            this.templateContentTreeView.setRoot(root);

            final File[] children = this.templateEngine.getWorkingDirectory().listFiles();
            if (children != null) {

                for (File child : children) {
                    this.templateContentTreeView.appendContentToTreeView(child, root);
                }
            }

            this.templateContentTreeView.closeItem(root);
            root.setExpanded(true);
        }
    }

    @Override
    public Parent getRoot() {
        return this.templateBuilder;
    }

    @Override
    public void postInitialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the tree view
        this.templateContentTreeView.setOnItemClick(event -> {
            final boolean consumeEvent = event.getClickCount() == 2
                    && event.getButton().equals(MouseButton.PRIMARY)
                    && event.getSource() instanceof FileTreeCell;

            if (consumeEvent) {
                File file = ((FileTreeCell) event.getSource()).getItem();

                if (file.isFile()) {

                    /**
                     * Check if the file is already opened and select it if it is,
                     otherwise open it.
                     */
                    Optional<IFileEditor> editor = this.openedFiles.getTabs()
                            .stream()
                            .filter(tab -> tab instanceof IFileEditor)
                            .map(tab -> (IFileEditor) tab)
                            .filter(tab -> tab.getFile().equals(file))
                            .findFirst();

                    if (editor.isPresent()) {
                        this.openedFiles.getSelectionModel().select((Tab) editor.get());
                    } else {

                        // The type of editor has to be determined
                        IFileEditor fileEditor;

                        // The file is the configuration file
                        if (file.equals(new File(this.templateEngine.getWorkingDirectory(), this.templateEngine.getConfigurationFilename()))) {
                            fileEditor = new ConfigurationFileEditor(this.templateEngine.getWorkingDirectory().toPath(), file);
                        } else {
                            /**
                             * Try to determine the best file editor to use
                             * by checking the MIME type
                             */
                            try {
                                String mimeType = Files.probeContentType(file.toPath());

                                if (mimeType != null && mimeType.contains("image"))
                                    fileEditor = new ImageFileEditor();
                                else fileEditor = new ACEFileEditor();

                                fileEditor.setWorkingPath(this.templateEngine.getWorkingDirectory().toPath());
                                fileEditor.setFile(file);
                            } catch (IOException e) {
                                LOGGER.log(Level.WARNING, "An error occurred while truing to determine the MIME type of the file to open", e);
                                fileEditor = new SimpleFileEditor();
                            }
                        }

                        this.openedFiles.getTabs().add((Tab) fileEditor);
                        this.openedFiles.getSelectionModel().selectLast();
                    }
                }
            }
        });
    }
}
