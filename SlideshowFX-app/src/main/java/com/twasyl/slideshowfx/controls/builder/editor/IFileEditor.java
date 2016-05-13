package com.twasyl.slideshowfx.controls.builder.editor;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.io.File;
import java.nio.file.Path;

/**
 * This class represents the concept of a file editor. It is used to for example to modify and create template files.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface IFileEditor<T extends Node> {

    /**
     * Get the Path that corresponds to the working directory of the template being used.
     * @return The Path of the working directory.
     */
    ObjectProperty<Path> workingPathProperty();

    /**
     * Get the Path that corresponds to the working directory of the template being used.
     * @return The Path of the working directory.
     */
    Path getWorkingPath();

    /**
     * Set the Path that corresponds to the working directory of the template being used.
     * @param workingPath The Path of the working directory.
     */
    void setWorkingPath(Path workingPath);

    /**
     * The file to be edited with the editor.
     * @return The property containing the file to be edited with the editor.
     */
    ObjectProperty<File> fileProperty();

    /**
     * The file to be edited with the editor.
     * @return The file to be edited with the editor.
     */
    File getFile();

    /**
     * Set the file to be edited with this editor.
     * @param file The new file edited by this editor.
     */
    void setFile(File file);

    /**
     * The node which contains the content of the file.
     * @return The property containing the node with the file's content.
     */
    ObjectProperty<T> fileContentProperty();

    /**
     * Get the node which contains the content of the file.
     * @return the node which contains the content of the file.
     */
    T getFileContent();

    /**
     * Set the node which contains the content of the file.
     * @param fileContent the new node which contains the file's content.
     */
    void setFileContent(T fileContent);

    /**
     * The ScrollPane that is used to display the {@link #fileContentProperty()}.
     * If the ScrollPane is not set, the {@link #fileContentProperty()} will be added directly to this editor.
     * @return The ScrollPane that is used to display the {@link #fileContentProperty()}
     */
    ObjectProperty<ScrollPane> editorScrollPaneProperty();

    /**
     * The ScrollPane that is used to display the {@link #fileContentProperty()}.
     * If the ScrollPane is not set, the {@link #fileContentProperty()} will be added directly to this editor.
     * @return The ScrollPane that is used to display the {@link #fileContentProperty()}
     */
    ScrollPane getEditorScrollPane();

    /**
     * Set the ScrollPane that is used to display the {@link #fileContentProperty()}.
     * If the ScrollPane is not set, the {@link #fileContentProperty()} will be added directly to this editor.
     * @param editorScrollPane The ScrollPane to use to display the {@link #fileContentProperty()}
     */
    void setEditorScrollPane(ScrollPane editorScrollPane);

    /**
     * Updates the content of the editor with the content of {@link #fileProperty}.
     *
     * @throws java.lang.NullPointerException if the given file is null
     * @throws java.lang.IllegalArgumentException if the given file can not be read
     */
    void updateFileContent();

    /**
     * This methods save the current content of the editor to the file stored in the {@link #fileProperty()}.
     * @throws java.lang.NullPointerException if the fileProperty is null.
     */
    void saveContent();
}
