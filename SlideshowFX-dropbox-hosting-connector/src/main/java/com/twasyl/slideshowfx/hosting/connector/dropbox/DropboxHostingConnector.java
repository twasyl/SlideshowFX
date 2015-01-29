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

package com.twasyl.slideshowfx.hosting.connector.dropbox;

import com.dropbox.core.*;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
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
public class DropboxHostingConnector extends AbstractHostingConnector {
    private static final Logger LOGGER = Logger.getLogger(DropboxHostingConnector.class.getName());

    private final DbxAppInfo appInfo;
    private final DbxRequestConfig dropboxConfiguration = new DbxRequestConfig("SlideshowFX", Locale.getDefault().toString());

    public DropboxHostingConnector() {
        super("dropbox", "Dropbox", new RemoteFile(null));

        this.appInfo = new DbxAppInfo(
                GlobalConfiguration.getProperty(this.CONSUMER_KEY),
                GlobalConfiguration.getProperty(this.CONSUMER_SECRET));

        this.accessToken = GlobalConfiguration.getProperty(this.ACCESS_TOKEN);
    }

    @Override
    public boolean authenticate() {
        // Prepare the request
        final DbxWebAuthNoRedirect authentication = new DbxWebAuthNoRedirect(this.dropboxConfiguration, this.appInfo);

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);

        // Listening for the div containing the access code to be displayed
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                final Element element = browser.getEngine().getDocument().getElementById("auth-code");
                if(element != null) {
                    try {
                        final DbxAuthFinish authenticationFinish = authentication.finish(element.getTextContent());
                        this.accessToken = authenticationFinish.accessToken;
                    } catch (DbxException e) {
                        LOGGER.log(Level.SEVERE, "Can not finish authentication", e);
                        this.accessToken = null;
                    } finally {
                        GlobalConfiguration.setProperty(this.ACCESS_TOKEN, this.accessToken);
                        stage.close();
                    }
                }

            }
        });

        browser.getEngine().load(authentication.start());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Dropbox");
        stage.showAndWait();

        return this.isAuthenticated();
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
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws FileNotFoundException {
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
        }
    }

    @Override
    public File download(File destination, RemoteFile file) {
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

        }

        return result;
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) {
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
                                return entry.isFolder() || (entry.isFile() && entry.name.endsWith(".sfx"));
                            } else if(includeFolders && !includePresentations) {
                                return entry.isFolder();
                            } else if(!includeFolders && includePresentations) {
                                return entry.isFolder() && entry.name.endsWith(".sfx");
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
        }

        return folders;
    }

    @Override
    public boolean fileExists(PresentationEngine engine, RemoteFile destination) {
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
        }

        return exist;
    }
}
