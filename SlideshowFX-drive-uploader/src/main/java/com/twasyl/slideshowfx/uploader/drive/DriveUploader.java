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

package com.twasyl.slideshowfx.uploader.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.uploader.AbstractUploader;
import com.twasyl.slideshowfx.uploader.drive.io.GoogleFile;
import com.twasyl.slideshowfx.uploader.io.RemoteFile;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.html.HTMLInputElement;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class DriveUploader extends AbstractUploader {
    private static final Logger LOGGER = Logger.getLogger(DriveUploader.class.getName());

    private GoogleCredential credential;

    public DriveUploader() { super("googledrive", "Google Drive", new GoogleFile()); }

    @Override
    public boolean authenticate() {

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = new JacksonFactory();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                AbstractUploader.getProperty(this.getCode().concat(".consumer.key")),
                AbstractUploader.getProperty(this.getCode().concat(".consumer.secret")),
                Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto")
                .build();

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if(newState == Worker.State.SUCCEEDED) {

                final HTMLInputElement codeElement = (HTMLInputElement) browser.getEngine().getDocument().getElementById("code");
                if(codeElement != null) {
                    final String authorizationCode = codeElement.getValue();

                    try {
                        final GoogleTokenResponse response = flow.newTokenRequest(authorizationCode.toString())
                                .setRedirectUri(AbstractUploader.getProperty(this.getCode().concat(".redirecturi")))
                                .execute();

                        this.credential = new GoogleCredential().setFromTokenResponse(response);
                        this.authenticated = true;
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to get access token", e);
                        this.authenticated = false;
                    } finally {
                        stage.close();
                    }

                }
            }
        });
        browser.getEngine().load(flow.newAuthorizationUrl()
                                    .setRedirectUri(AbstractUploader.getProperty(this.getCode().concat(".redirecturi")))
                                    .build());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Google Drive");
        stage.showAndWait();

        return this.authenticated;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void upload(PresentationEngine engine) throws FileNotFoundException {
        this.upload(engine, this.getRootFolder());
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder) throws FileNotFoundException {
        if(this.isAuthenticated()) {
            com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
            body.setTitle(engine.getArchive().getName());
            body.setMimeType("application/zip");
            body.setParents(Arrays.asList(new ParentReference()
                                                    .setId(((GoogleFile) folder).getId())
                                         ));

            FileContent mediaContent = new FileContent("application/zip", engine.getArchive());

            Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();
            try {
                service.files().insert(body, mediaContent).execute();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not upload presentation to Google Drive", e);
            }
        }

    }

    @Override
    public List<RemoteFile> getSubfolders(RemoteFile parent) {
        if(parent == null) throw new NullPointerException("The parent can not be null");
        if(!(parent instanceof GoogleFile)) throw new IllegalArgumentException("The given parent must be a GoogleFile");

        final List<RemoteFile> folders = new ArrayList<>();

        if(this.isAuthenticated()) {
            final Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();

            try {
                ChildList children = service.children().list(((GoogleFile) parent).getId()).execute();
                GoogleFile child;

                for(ChildReference reference : children.getItems()) {

                    // Execute a request to get the file info
                    com.google.api.services.drive.model.File fileInfo = null;
                    try {
                        fileInfo = service.files().get(reference.getId()).execute();

                        // This MIME type identifies a folder on Google Drive and also ensure the folder is not trashed
                        if("application/vnd.google-apps.folder".equals(fileInfo.getMimeType())
                                && (fileInfo.getLabels().getTrashed() == null
                                    || !fileInfo.getLabels().getTrashed())) {
                            child = new GoogleFile((GoogleFile) parent, fileInfo.getTitle(), fileInfo.getId());
                            folders.add(child);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Can not determine type of file on Google Drive", e);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not list folders of Google Drive", e);
            }
        }

        return folders;
    }
}
