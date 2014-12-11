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
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.uploader.AbstractUploader;
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

    private final String APP_KEY = "<APP_KEY>";
    private final String APP_SECRET = "<APP_SECRET>";

    private final DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
    private final DbxRequestConfig dropboxConfiguration = new DbxRequestConfig("SlideshowFX", Locale.getDefault().toString());

    public DropboxUploader() { super("Dropbox"); }

    @Override
    public boolean authenticate() {
        final DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(this.dropboxConfiguration, appInfo);

        final StringBuilder builder = new StringBuilder();

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {
                final Element divAuth = browser.getEngine().getDocument().getElementById("auth-code");
                if (divAuth != null) {
                    builder.append(divAuth.getTextContent());
                    stage.close();
                }
            }
        });
        browser.getEngine().load(webAuth.start());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Dropbox");
        stage.showAndWait();

        try {
            final DbxAuthFinish authFinish = webAuth.finish(builder.toString());
            this.accessToken = authFinish.accessToken;
            this.authenticated = true;
        } catch (DbxException e) {
            LOGGER.log(Level.SEVERE, "Error while authenticating to Dropbox", e);
            this.authenticated = false;
        }

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
        this.upload(engine, new File("/"));
    }

    @Override
    public void upload(PresentationEngine engine, File folder) throws FileNotFoundException {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive to upload can not be null");
        if(!engine.getArchive().exists()) throw new FileNotFoundException("The archive to upload does not exist");

        if(this.isAuthenticated()) {
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);

            try(final InputStream archiveStream = new FileInputStream(engine.getArchive())) {
                final DbxEntry.File dropboxUpload = client.uploadFile(
                        new File(folder, engine.getArchive().getName()).getPath(),
                        DbxWriteMode.add(),
                        engine.getArchive().length(),
                        archiveStream);
            } catch (DbxException | IOException e) {
                LOGGER.log(Level.SEVERE, "Error while trying to upload the presentation", e);
            }
        }
    }

    @Override
    public List<File> getFolders() {
        final List<File> folders = new ArrayList<>();

        if(this.isAuthenticated()) {
            folders.add(new File("/"));
            final DbxClient client = new DbxClient(this.dropboxConfiguration, this.accessToken);
            final List<DbxEntry> listing;

            try {
                listing = client.searchFileAndFolderNames("/", "/");
                listing.stream()
                        .filter(entry -> entry.isFolder())
                        .forEach(entry -> folders.add(new File(entry.path)));
            } catch (DbxException e) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
            }
        }
        return folders;
    }
}
