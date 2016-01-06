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

package com.twasyl.slideshowfx.hosting.connector.dropbox;

import com.dropbox.core.*;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.BasicHostingConnectorOptions;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This connector allows to interact with Dropbox.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DropboxHostingConnector extends AbstractHostingConnector<BasicHostingConnectorOptions> {
    private static final Logger LOGGER = Logger.getLogger(DropboxHostingConnector.class.getName());

    private DbxAppInfo appInfo;
    private final DbxRequestConfig dropboxConfiguration = new DbxRequestConfig("SlideshowFX", Locale.getDefault().toString());

    public DropboxHostingConnector() {
        super("dropbox", "Dropbox", new RemoteFile(null));

        this.setOptions(new BasicHostingConnectorOptions());

        String configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(CONSUMER_KEY_PROPERTY_SUFFIX));
        if(configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setConsumerKey(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(CONSUMER_SECRET_PROPERTY_SUFFIX));
        if(configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setConsumerSecret(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(REDIRECT_URI_PROPERTY_SUFFIX));
        if(configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setRedirectUri(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX));
        if(configuration != null && !configuration.trim().isEmpty()) {
            this.accessToken = configuration;
        }

        if(this.getOptions().getConsumerKey() != null && this.getOptions().getConsumerSecret() != null) {
            this.appInfo = new DbxAppInfo(this.getOptions().getConsumerKey(), this.getOptions().getConsumerSecret());
        }
    }

    @Override
    public Node getConfigurationUI() {
        this.newOptions = new BasicHostingConnectorOptions();
        this.newOptions.setConsumerKey(this.getOptions().getConsumerKey());
        this.newOptions.setConsumerSecret(this.getOptions().getConsumerSecret());
        this.newOptions.setRedirectUri(this.getOptions().getRedirectUri());

        final Label consumerKeyLabel = new Label("Consumer key:");
        final Label consumerSecretLabel = new Label("Consumer secret:");
        final Label redirectUriLabel = new Label("Redirect URI:");

        final TextField consumerKeyTextField = new TextField();
        consumerKeyTextField.textProperty().bindBidirectional(this.newOptions.consumerKeyProperty());
        consumerKeyTextField.setPrefColumnCount(20);

        final TextField consumerSecretTextField = new TextField();
        consumerSecretTextField.textProperty().bindBidirectional(this.newOptions.consumerSecretProperty());
        consumerSecretTextField.setPrefColumnCount(20);

        final TextField redirectUriTextField = new TextField();
        redirectUriTextField.textProperty().bindBidirectional(this.newOptions.redirectUriProperty());
        redirectUriTextField.setPrefColumnCount(20);

        final HBox consumerKeyBox = new HBox(5, consumerKeyLabel, consumerKeyTextField);
        consumerKeyBox.setAlignment(Pos.BASELINE_LEFT);

        final HBox consumerSecretBox = new HBox(5, consumerSecretLabel, consumerSecretTextField);
        consumerSecretBox.setAlignment(Pos.BASELINE_LEFT);

        final HBox redirectUriBox = new HBox(5, redirectUriLabel, redirectUriTextField);
        redirectUriBox.setAlignment(Pos.BASELINE_LEFT);

        final VBox container = new VBox(5, consumerKeyBox, consumerSecretBox, redirectUriBox);

        return container;
    }

    @Override
    public void saveNewOptions() {
        if(this.getNewOptions() != null) {
            this.setOptions(this.getNewOptions());

            if (this.getOptions().getConsumerKey() != null) {
                GlobalConfiguration.setProperty(getConfigurationBaseName().concat(CONSUMER_KEY_PROPERTY_SUFFIX),
                        this.getOptions().getConsumerKey());
            }

            if (this.getOptions().getConsumerSecret() != null) {
                GlobalConfiguration.setProperty(getConfigurationBaseName().concat(CONSUMER_SECRET_PROPERTY_SUFFIX),
                        this.getOptions().getConsumerSecret());
            }

            if (this.getOptions().getRedirectUri() != null) {
                GlobalConfiguration.setProperty(getConfigurationBaseName().concat(REDIRECT_URI_PROPERTY_SUFFIX),
                        this.getOptions().getRedirectUri());
            }

            if(this.getOptions().getConsumerKey() != null && this.getOptions().getConsumerSecret() != null) {
                this.appInfo = new DbxAppInfo(this.getOptions().getConsumerKey(), this.getOptions().getConsumerSecret());
            }
        }
    }

    @Override
    public void authenticate() throws HostingConnectorException {
        if(this.appInfo == null) throw new HostingConnectorException(HostingConnectorException.MISSING_CONFIGURATION);

        // Prepare the request
        final DbxWebAuthNoRedirect authentication = new DbxWebAuthNoRedirect(this.dropboxConfiguration, this.appInfo);

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);

        // Listening for the div containing the access code to be displayed
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                final Element element = browser.getEngine().getDocument().getElementById("auth-code");
                if (element != null) {
                    try {
                        final DbxAuthFinish authenticationFinish = authentication.finish(element.getTextContent());
                        this.accessToken = authenticationFinish.accessToken;
                    } catch (DbxException e) {
                        LOGGER.log(Level.SEVERE, "Can not finish authentication", e);
                        this.accessToken = null;
                    } finally {
                        if(this.accessToken != null) {
                            GlobalConfiguration.setProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX), this.accessToken);
                        }
                        stage.close();
                    }
                }

            }
        });

        browser.getEngine().load(authentication.start());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Dropbox");
        stage.showAndWait();

        if(!this.isAuthenticated()) throw new HostingConnectorException(HostingConnectorException.AUTHENTICATION_FAILURE);
    }

    @Override
    public boolean checkAccessToken() {
        boolean valid = false;

        final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
        try {
            client.getAccountInfo();
            valid = true;
        } catch (DbxException e) {
            LOGGER.log(Level.WARNING, "Can not determine if access token is valid", e);
        }

        return valid;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws HostingConnectorException, FileNotFoundException {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive to upload can not be null");
        if(!engine.getArchive().exists()) throw new FileNotFoundException("The archive to upload does not exist");

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);

            DbxWriteMode writeMode = null;
            final StringBuilder fileName = new StringBuilder();

            if(overwrite) {
                try {
                    final DbxEntry entry = client.getMetadata(folder.toString().concat("/".concat(engine.getArchive().getName())));
                    fileName.append(engine.getArchive().getName());

                    // Ensure the file has been found.
                    if(entry != null) {
                        writeMode = DbxWriteMode.update(((DbxEntry.File) entry).rev);
                    } else {
                        writeMode = DbxWriteMode.add();
                    }
                } catch (DbxException e) {
                    LOGGER.log(Level.SEVERE, "Can not get file metadata");
                    writeMode = writeMode.add();
                }
            } else {
                writeMode = DbxWriteMode.add();
                if(this.fileExists(engine, folder)) {
                    fileName.append(engine.getArchive().getName())
                            .append(String.format(" %1$tF %1$tT"))
                            .append(".").append(engine.getArchiveExtension());
                } else {
                    fileName.append(engine.getArchive().getName());
                }
            }

            try(final InputStream archiveStream = new FileInputStream(engine.getArchive())) {
                client.uploadFile(
                        new RemoteFile(folder, fileName.toString()).toString(),
                        writeMode,
                        engine.getArchive().length(),
                        archiveStream);
            } catch (DbxException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error while trying to upload the presentation", e);
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }
    }

    @Override
    public File download(File destination, RemoteFile file) throws HostingConnectorException {
        if(destination == null) throw new NullPointerException("The destination can not be null");
        if(file == null) throw new NullPointerException("The file to download can not be null");
        if(!destination.isDirectory()) throw new IllegalArgumentException("The destination is not a folder");

        File result = null;

        if(this.isAuthenticated()) {
            result = new File(destination, file.getName());

            try(final OutputStream out = new FileOutputStream(result)) {
                final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);

                client.getFile(file.toString(), null, out);
            } catch (IOException | DbxException e) {
                LOGGER.log(Level.SEVERE, "Can not download the file", e);
                result = null;
            }

        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return result;
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) throws HostingConnectorException {
        if(parent == null) throw new NullPointerException("The parent can not be null");

        final List<RemoteFile> folders = new ArrayList<>();

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
            final DbxEntry.WithChildren listing;

            try {
                listing = client.getMetadataWithChildren(parent.toString());
                listing.children
                        .stream()
                        .filter(entry -> {
                            if(includeFolders && includePresentations) {
                                return entry.isFolder() || (entry.isFile() && entry.name.endsWith(PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION));
                            } else if(includeFolders && !includePresentations) {
                                return entry.isFolder();
                            } else if(!includeFolders && includePresentations) {
                                return entry.isFolder() && entry.name.endsWith(PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION);
                            } else return false;
                        })
                        .forEach(entry ->
                            folders.add(new RemoteFile(parent, entry.name)
                                            .setFile(entry.isFile())
                                            .setFolder(entry.isFolder()))
                        );
            } catch (DbxException e) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return folders;
    }

    @Override
    public boolean fileExists(PresentationEngine engine, RemoteFile destination) throws HostingConnectorException {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if(destination == null) throw new NullPointerException("The destination can not be null");

        boolean exist = true;

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
            final DbxEntry.WithChildren listing;

            try {
                listing = client.getMetadataWithChildren(destination.toString());
                exist = listing.children
                        .stream()
                        .filter(entry -> entry.isFile() && engine.getArchive().getName().equals(entry.name))
                        .count() > 0;
            } catch (DbxException e) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return exist;
    }
}
