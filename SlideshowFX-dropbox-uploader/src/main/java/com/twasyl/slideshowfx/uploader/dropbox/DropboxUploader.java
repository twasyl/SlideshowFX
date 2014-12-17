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

package com.twasyl.slideshowfx.uploader.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.json.JsonReader;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.uploader.AbstractUploader;
import com.twasyl.slideshowfx.uploader.io.RemoteFile;
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
 * This uploader allows to upload presentations to Dropbox.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class DropboxUploader extends AbstractUploader {
    private static final Logger LOGGER = Logger.getLogger(DropboxUploader.class.getName());

    private final DbxAppInfo appInfo;
    private final DbxRequestConfig dropboxConfiguration = new DbxRequestConfig("SlideshowFX", Locale.getDefault().toString());

    public DropboxUploader() {
        super("dropbox", "Dropbox", new RemoteFile(null));

        this.appInfo = new DbxAppInfo(
                AbstractUploader.getProperty(this.getCode().concat(".consumer.key")),
                AbstractUploader.getProperty(this.getCode().concat(".consumer.secret")));
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
                        this.authenticated = true;
                    } catch (DbxException e) {
                        LOGGER.log(Level.SEVERE, "Can not finish authentication", e);
                        this.authenticated = false;
                    } finally {
                        stage.close();
                    }
                }

            }
        });

        browser.getEngine().load(authentication.start());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Dropbox");
        stage.showAndWait();

        return this.authenticated;
    }

    @Override
    public void disconnect() {
        if(isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
            try {
                client.disableAccessToken();
            } catch (DbxException e) {
                LOGGER.log(Level.WARNING, "Error while trying to disconnect from Dropbox", e);
            }
        }
    }

    @Override
    public void upload(PresentationEngine engine) throws FileNotFoundException {
        this.upload(engine, this.getRootFolder());
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder) throws FileNotFoundException {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive to upload can not be null");
        if(!engine.getArchive().exists()) throw new FileNotFoundException("The archive to upload does not exist");

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);

            try(final InputStream archiveStream = new FileInputStream(engine.getArchive())) {
                final DbxEntry.File dropboxUpload = client.uploadFile(
                        new RemoteFile(folder, engine.getArchive().getName()).toString(),
                        DbxWriteMode.add(),
                        engine.getArchive().length(),
                        archiveStream);
            } catch (DbxException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error while trying to upload the presentation", e);
            }
        }
    }

    @Override
    public List<RemoteFile> getSubfolders(RemoteFile parent) {
        if(parent == null) throw new NullPointerException("The parent can not be null");

        final List<RemoteFile> folders = new ArrayList<>();

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
            final DbxEntry.WithChildren listing;

            try {
                listing = client.getMetadataWithChildren(parent.toString());
                listing.children
                        .stream()
                        .filter(entry -> entry.isFolder())
                        .forEach(entry -> folders.add(new RemoteFile(parent, entry.name)));
            } catch (DbxException e) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
            }
        }

        return folders;
    }


}
