/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.controls.builder.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the default behavior of a file editor.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractFileEditor<T extends Node> extends Tab implements IFileEditor<T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractFileEditor.class.getName());
    protected final ObjectProperty<Path> workingPath = new SimpleObjectProperty<>();
    protected final ObjectProperty<File> file = new SimpleObjectProperty<>();
    protected final ObjectProperty<T> fileContent = new SimpleObjectProperty<>();
    protected final ObjectProperty<ScrollPane> editorScrollPane = new SimpleObjectProperty<>();

    public AbstractFileEditor() {
        this.setTooltip(new Tooltip());

        this.fileProperty().addListener((value, oldFile, newFile) -> {
            if (newFile != null && !newFile.equals(oldFile) && newFile.canRead()) {
                this.updateFileContent();

                this.setText(newFile.getName());
                this.getTooltip().setText(newFile.getAbsolutePath());
            }
        });

        this.fileContent.addListener((value, oldValue, newValue) -> {
            // Removes the old content from the editor, just to be sure
            if(oldValue != null) {
                if(this.getEditorScrollPane() != null && this.getEditorScrollPane().getContent() == oldValue) {
                    this.getEditorScrollPane().setContent(null);
                } else {
                    this.setContent(null);
                }
            }

            if(newValue != null) {
                if(this.getEditorScrollPane() != null) this.getEditorScrollPane().setContent(newValue);
                else this.setContent(newValue);
            }
        });

        this.editorScrollPane.addListener((value, oldValue, newValue) -> {
            // Erase previous content
            if(oldValue != null) oldValue.setContent(null);

            this.setContent(null);

            if(this.getFileContent() != null) {
                if(newValue != null) {
                    newValue.setContent(this.getFileContent());
                    this.setContent(newValue);
                } else {
                    this.setContent(this.getFileContent());
                }
            }
        });
    }

    public AbstractFileEditor(File file) {
        this();
        this.file.set(file);
    }

    @Override
    public ObjectProperty<Path> workingPathProperty() { return workingPath; }

    @Override
    public Path getWorkingPath() { return workingPath.get(); }

    @Override
    public void setWorkingPath(Path workingPath) {
        try {
            this.workingPath.set(workingPath.toRealPath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not determine real path", e);
        }}

    @Override
    public ObjectProperty<File> fileProperty() { return this.file; }

    @Override
    public File getFile() { return this.file.get(); }

    @Override
    public void setFile(File file) {
        this.file.set(file);
    }

    @Override
    public ObjectProperty<T> fileContentProperty() { return this.fileContent; }

    @Override
    public T getFileContent() { return this.fileContent.get(); }

    @Override
    public void setFileContent(T fileContent) { this.fileContent.set(fileContent); }

    @Override
    public ObjectProperty<ScrollPane> editorScrollPaneProperty() { return this.editorScrollPane; }

    @Override
    public ScrollPane getEditorScrollPane() { return this.editorScrollPane.get(); }

    @Override
    public void setEditorScrollPane(ScrollPane editorScrollPane) { this.editorScrollPane.set(editorScrollPane); }
}
