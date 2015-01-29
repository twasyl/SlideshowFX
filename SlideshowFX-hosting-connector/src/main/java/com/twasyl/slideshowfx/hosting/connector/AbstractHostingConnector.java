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

package com.twasyl.slideshowfx.hosting.connector;

import com.twasyl.slideshowfx.controls.Dialog;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.FileNotFoundException;

/**
 * The base class for implementing an {@link IHostingConnector}.
 *
 *  @author Thierry Wasylczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public abstract class AbstractHostingConnector implements IHostingConnector {
    /*
     * Constants for stored properties
     */
    private static final String PROPERTIES_PREFIX = "hosting.connector.";

    /*
     * Constants for stored properties
     */
    protected final String CONSUMER_KEY;
    protected final String CONSUMER_SECRET;
    protected final String ACCESS_TOKEN;
    protected final String REDIRECT_URI;

    protected final String code;
    protected final String name;
    protected String accessToken;
    protected final RemoteFile rootFolder;

    protected AbstractHostingConnector(String code, String name, RemoteFile rootFolder) {
        this.code = code;
        this.name = name;
        this.rootFolder = rootFolder;

        this.CONSUMER_KEY = PROPERTIES_PREFIX.concat(this.code).concat(".consumer.key");
        this.CONSUMER_SECRET = PROPERTIES_PREFIX.concat(this.code).concat(".consumer.secret");
        this.ACCESS_TOKEN = PROPERTIES_PREFIX.concat(this.code).concat(".accesstoken");
        this.REDIRECT_URI = PROPERTIES_PREFIX.concat(this.code).concat(".redirecturi");
    }

    @Override
    public String getCode() { return this.code; }

    @Override
    public String getName() { return this.name; }

    @Override
    public boolean isAuthenticated() {
        boolean authenticated = false;

        if(this.accessToken != null && !this.accessToken.trim().isEmpty()) {
            authenticated = checkAccessToken();
        }

        return authenticated;
    }

    @Override
    public String getAccessToken() { return this.accessToken; }

    @Override
    public RemoteFile getRootFolder() { return this.rootFolder; }

    @Override
    public void upload(PresentationEngine engine) throws FileNotFoundException {
        this.upload(engine, this.getRootFolder(), false);
    }

    @Override
    public RemoteFile chooseFile(boolean showFolders, boolean showFiles) {

        final TreeItem<RemoteFile> rootItem = this.buildCustomTreeItem(this.getRootFolder(), showFolders, showFiles);
        /**
         * Get the subfolders of root and populate the root TreeItem
         */
        this.list(this.getRootFolder(), showFolders, showFiles)
                .stream()
                .forEach(subfolder -> rootItem.getChildren().add(this.buildCustomTreeItem(subfolder, showFolders, showFiles)));

        final TreeView<RemoteFile> treeView = this.buildCustomTreeView(rootItem);

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
     * Build the {@code TreeView<RemoteFile>} that will host the list of folders available remotely.
     * If the given {@code root} is null, it will not be added as root of the created TreeView.
     *
     * @param root The root that will be added to the created TreeView.
     * @return The TreeView to host remote folders.
     */
    private TreeView<RemoteFile> buildCustomTreeView(final TreeItem<RemoteFile> root) {
        final TreeView<RemoteFile> treeView = new TreeView<>();
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        /**
         * Because the default implementation of a CellFactory calls the toString() method of the value, another
         * implementation is created in order to simply display the {@link com.twasyl.slideshowfx.hosting.connector.io.RemoteFile#getName()}.
         */
        treeView.setCellFactory(targetTreeView -> {
            final TreeCell<RemoteFile> cell = new TreeCell<RemoteFile>() {
                @Override
                protected void updateItem(RemoteFile item, boolean empty) {
                    super.updateItem(item, empty);

                    if(!empty && item != null) setText(item.isRoot() ? "/" : item.getName());
                    else setText(null);
                }
            };

            return cell;
        });
        treeView.setPrefSize(500, 400);

        if(root != null) treeView.setRoot(root);

        return treeView;
    }

    /**
     * This method creates a {@code TreeItem<File>} that loads its children from the service when it is expanded.
     *
     * @param value The value of the item.
     * @return The created TreeItem.
     */
    private TreeItem<RemoteFile> buildCustomTreeItem(RemoteFile value, boolean showFolders, boolean showFiles) {
        final TreeItem<RemoteFile> item = new TreeItem<RemoteFile>(value) {
            @Override
            public boolean isLeaf() {
                // Allows to always display the arrow for expending the folder.
                return value.isFile();
            }
        };

        item.expandedProperty().addListener((expandedValue, oldExpanded, newExpanded) -> {
            if(newExpanded && item.getChildren().isEmpty()) {
                for(RemoteFile child : this.list(value, showFolders, showFiles)) {
                    item.getChildren().add(this.buildCustomTreeItem(child, showFolders, showFiles));
                }
            }
        });

        return item;
    }
}
