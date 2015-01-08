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

package com.twasyl.slideshowfx.hosting.connector;

import com.twasyl.slideshowfx.controls.Dialog;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for implementing an {@link IHostingConnector}.
 *
 *  @author Thierry Wasylczenko
 *  @version 1.0
 *  @since SlideshowFX 1.0.0
 */
public abstract class AbstractHostingConnector implements IHostingConnector {
    private static final Logger LOGGER = Logger.getLogger(AbstractHostingConnector.class.getName());
    private static final File CONFIG_FILE = new File(System.getProperty("user.home"), ".SlideshowFX/.slideshowfx.hosting.connector.properties");

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

        this.CONSUMER_KEY = this.code.concat(".consumer.key");
        this.CONSUMER_SECRET = this.code.concat(".consumer.secret");
        this.ACCESS_TOKEN = this.code.concat(".accesstoken");
        this.REDIRECT_URI = this.code.concat(".redirecturi");
    }

    /**
     * Get a property from the file containing all hosting connector properties. This methods return {@code null} is the property
     * is not found or if the configuration file does not exist.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The value of the property or {@code null} if it is not found or the configuration does not exist.
     * @throws java.lang.NullPointerException If the property name is null.
     * @throws java.lang.IllegalArgumentException If the property name is empty.
     */
    protected final static String getProperty(final String propertyName) {
        if(propertyName == null) throw new NullPointerException("The property name can not be null");
        if(propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");

        String value = null;

        if(CONFIG_FILE.exists()) {
            final Properties properties = new Properties();

            try(final Reader reader = new FileReader(CONFIG_FILE)) {
                properties.load(reader);
                value = properties.getProperty(propertyName.trim());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file", e);
            }
        }

        return value;
    }

    /**
     * Save the given {@code propertyName} and {@code propertyValue} to the configuration file.
     *
     * @param propertyName The name of the property to save.
     * @param propertyValue The value of the property to save.
     * @throws java.lang.NullPointerException If the name or value of the property is null.
     * @throws java.lang.IllegalArgumentException If the name or value of the property is empty.
     */
    protected final static void setProperty(final String propertyName, final String propertyValue) {
        if(propertyName == null) throw new NullPointerException("The property name can not be null");
        if(propertyValue == null) throw new NullPointerException("The property value can not be null");
        if(propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");
        if(propertyValue.trim().isEmpty()) throw new IllegalArgumentException("The property value can not be empty");

        final Properties properties = new Properties();

        // Load the current properties if they exist
        if(CONFIG_FILE.exists()) {
            try(final Reader reader = new FileReader(CONFIG_FILE)) {
                properties.load(reader);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file", e);
            }
        }

        // Add the property
        properties.put(propertyName.trim(), propertyValue);

        // Store everything
        try(final Writer writer = new FileWriter(CONFIG_FILE)) {
            properties.store(writer, "");
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save configuration", e);
        }
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
