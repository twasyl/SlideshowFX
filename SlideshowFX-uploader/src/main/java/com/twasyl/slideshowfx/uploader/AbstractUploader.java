/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.uploader;

import com.twasyl.slideshowfx.controls.Dialog;
import com.twasyl.slideshowfx.uploader.io.RemoteFile;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

import java.io.File;

/**
 * The base class for implementing an {@link com.twasyl.slideshowfx.uploader.IUploader}.
 *
 *  @author Thierry Wasylczenko
 *  @version 1.0
 *  @since 1.0
 */
public abstract class AbstractUploader implements IUploader {
    protected boolean authenticated;
    protected String accessToken;
    protected final String name;
    protected final RemoteFile rootFolder;

    protected AbstractUploader(String name, RemoteFile rootFolder) {
        this.name = name;
        this.rootFolder = rootFolder;
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public boolean isAuthenticated() { return this.authenticated; }

    @Override
    public String getAccessToken() { return this.accessToken; }

    @Override
    public RemoteFile getRootFolder() { return this.rootFolder; }

    @Override
    public RemoteFile chooseDestinationFile() {


        final TreeItem<RemoteFile> rootItem = this.buildCustomTreeItem(this.getRootFolder());
        /**
         * Get the subfolders of root and populate the root TreeItem
         */
        this.getSubfolders(this.getRootFolder())
                .stream()
                .forEach(subfolder -> rootItem.getChildren().add(this.buildCustomTreeItem(subfolder)));

        final TreeView<RemoteFile> treeView = new TreeView<>(rootItem);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.setPrefSize(500, 400);

        final VBox content = new VBox(5);
        content.getChildren().addAll(new Label("Choose a destination:"), treeView);

        RemoteFile destination = null;
        final Dialog.Response answer = Dialog.showCancellableDialog(true, null, "Choose destination", content);

        if(answer == Dialog.Response.OK) {
            final TreeItem<RemoteFile> selection = treeView.getSelectionModel().getSelectedItem();
            if(selection != null) destination = selection.getValue();
        }

        return destination;
    }

    /**
     * This method creates a {@code TreeItem<File>} that loads its children from the service when it is expanded.
     *
     * @param value The value of the item.
     * @return The created TreeItem.
     */
    private TreeItem<RemoteFile> buildCustomTreeItem(RemoteFile value) {
        final TreeItem<RemoteFile> item = new TreeItem<RemoteFile>(value) {
            @Override
            public boolean isLeaf() {
                // Allows to always display the arrow for expending the folder.
                return false;
            }
        };

        item.expandedProperty().addListener((expandedValue, oldExpanded, newExpanded) -> {
            if(newExpanded && item.getChildren().isEmpty()) {
                for(RemoteFile child : this.getSubfolders(value)) {
                    item.getChildren().add(this.buildCustomTreeItem(child));
                }
            }
        });

        return item;
    }
}
