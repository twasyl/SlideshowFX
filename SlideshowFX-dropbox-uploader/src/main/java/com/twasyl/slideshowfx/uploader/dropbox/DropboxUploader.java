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

    private final String APP_KEY = "lua1gp2ria694by";

    private final DbxRequestConfig dropboxConfiguration = new DbxRequestConfig("SlideshowFX", Locale.getDefault().toString());

    public DropboxUploader() { super("Dropbox"); }

    @Override
    public boolean authenticate() {
        final String authenticationUrl = String.format("https://www.dropbox.com/1/oauth2/authorize?client_id=%1$s&response_type=token&redirect_uri=%2$s",
                APP_KEY,
                "https://slideshowfx-app");

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if(newState == Worker.State.FAILED
                    && browser.getEngine().getLocation().startsWith("https://slideshowfx-app")) {

                final String requestParamAccessToken = "access_token=";
                int paramIndex = browser.getEngine().getLocation().indexOf(requestParamAccessToken);

                if(paramIndex != -1) {
                    paramIndex += requestParamAccessToken.length();
                    final int endOfParamIndex = browser.getEngine().getLocation().indexOf("&", paramIndex);

                    this.accessToken = endOfParamIndex == -1 ?
                            browser.getEngine().getLocation().substring(paramIndex) :
                            browser.getEngine().getLocation().substring(paramIndex, endOfParamIndex);

                    this.authenticated = true;
                } else {
                    this.authenticated = false;
                }

                stage.close();
            }
        });
        browser.getEngine().load(authenticationUrl);

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
                        new File(folder, engine.getArchive().getName()).getPath().replaceAll("\\\\", "/"),
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
            this.fillFoldersList(folders, "/", client);
        }

        return folders;
    }

    /**
     * Fill the given {@code folders} list.
     * @param folders The list of folders that will be filled.
     * @param path The path that will serve for looking for folders
     * @param client The client used for requesting the Dropbox API.
     */
    private void fillFoldersList(final List<File> folders, final String path, final DbxClient client) {
        final DbxEntry.WithChildren listing;

        try {
            listing = client.getMetadataWithChildren(path);
            listing.children
                    .stream()
                    .filter(entry -> entry.isFolder())
                    .forEach(entry -> {
                        folders.add(new File(entry.path));
                        this.fillFoldersList(folders, entry.path, client);
                    });
        } catch (DbxException e) {
            LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
        }
    }
}
