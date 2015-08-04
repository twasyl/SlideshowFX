/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.hosting.connector;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.naming.AuthenticationException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for implementing an {@link IHostingConnector}.
 *
 *  @author Thierry Wasylczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public abstract class AbstractHostingConnector<T extends IHostingConnectorOptions> extends AbstractPlugin<T> implements IHostingConnector<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractHostingConnector.class.getName());

    /*
     * Constants for stored properties
     */
    private static final String PROPERTIES_PREFIX = "hosting.connector.";
    protected static final String CONSUMER_KEY_PROPERTY_SUFFIX = ".consumer.key";
    protected static final String CONSUMER_SECRET_PROPERTY_SUFFIX = ".consumer.secret";
    protected static final String REDIRECT_URI_PROPERTY_SUFFIX = ".redirecturi";
    protected static final String ACCESS_TOKEN_PROPERTY_SUFFIX = ".accesstoken";

    private final String configurationBaseName;

    protected T newOptions;
    protected final String code;
    protected String accessToken;
    protected final RemoteFile rootFolder;

    protected AbstractHostingConnector(String code, String name, RemoteFile rootFolder) {
        super(name);
        this.code = code;
        this.rootFolder = rootFolder;

        this.configurationBaseName = PROPERTIES_PREFIX.concat(this.code);
    }

    @Override
    public String getConfigurationBaseName() { return this.configurationBaseName; }

    @Override
    public T getNewOptions() { return this.newOptions; }

    @Override
    public String getCode() { return this.code; }

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
    public void upload(PresentationEngine engine) throws HostingConnectorException, FileNotFoundException {
        this.upload(engine, this.getRootFolder(), false);
    }

    @Override
    public RemoteFile chooseFile(boolean showFolders, boolean showFiles) throws HostingConnectorException {

        final TreeItem<RemoteFile> rootItem = this.buildCustomTreeItem(this.getRootFolder(), showFolders, showFiles);
        /**
         * Get the subfolders of root and populate the root TreeItem
         */
        final List<RemoteFile> subfolders = this.list(this.getRootFolder(), showFolders, showFiles);
        for(RemoteFile subfolder : subfolders) {
            rootItem.getChildren().add(this.buildCustomTreeItem(subfolder, showFolders, showFiles));
        }

        final TreeView<RemoteFile> treeView = this.buildCustomTreeView(rootItem);

        final VBox content = new VBox(5);
        content.getChildren().addAll(new Label("Choose a destination:"), treeView);

        RemoteFile destination = null;
        final ButtonType response = DialogHelper.showCancellableDialog("Choose destination", content);

        if(response != null && response == ButtonType.OK) {
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
    private TreeItem<RemoteFile> buildCustomTreeItem(RemoteFile value, boolean showFolders, boolean showFiles) throws HostingConnectorException{
        final TreeItem<RemoteFile> item = new TreeItem<RemoteFile>(value) {
            @Override
            public boolean isLeaf() {
                // Allows to always display the arrow for expending the folder.
                return value.isFile();
            }
        };

        item.expandedProperty().addListener((expandedValue, oldExpanded, newExpanded) -> {
            if(newExpanded && item.getChildren().isEmpty()) {
                try {
                    for(RemoteFile child : this.list(value, showFolders, showFiles)) {
                        item.getChildren().add(this.buildCustomTreeItem(child, showFolders, showFiles));
                    }
                } catch (HostingConnectorException e) {
                    LOGGER.log(Level.SEVERE, "Error while building the custom tree view", e);
                }
            }
        });

        return item;
    }
}
