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

package com.twasyl.slideshowfx.hosting.connector.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.drive.io.GoogleFile;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.html.HTMLInputElement;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This connector allows to interact with Google Drive.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class DriveHostingConnector extends AbstractHostingConnector {
    private static final Logger LOGGER = Logger.getLogger(DriveHostingConnector.class.getName());
    private static final String SLIDESHOWFX_MIME_TYPE = "application/slideshowfx";

    private GoogleCredential credential;

    public DriveHostingConnector() {
        super("googledrive", "Google Drive", new GoogleFile());

        this.accessToken = AbstractHostingConnector.getProperty(this.ACCESS_TOKEN);
    }

    @Override
    public boolean authenticate() {

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = new JacksonFactory();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                AbstractHostingConnector.getProperty(this.CONSUMER_KEY),
                AbstractHostingConnector.getProperty(this.CONSUMER_SECRET),
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
                                .setRedirectUri(AbstractHostingConnector.getProperty(this.REDIRECT_URI))
                                .execute();

                        this.credential = new GoogleCredential().setFromTokenResponse(response);
                        this.accessToken = this.credential.getAccessToken();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to get access token", e);
                        this.accessToken = null;
                    } finally {
                        AbstractHostingConnector.setProperty(this.ACCESS_TOKEN, this.accessToken);
                        stage.close();
                    }

                }
            }
        });
        browser.getEngine().load(flow.newAuthorizationUrl()
                                    .setRedirectUri(AbstractHostingConnector.getProperty(this.REDIRECT_URI))
                                    .build());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Google Drive");
        stage.showAndWait();

        return this.isAuthenticated();
    }

    @Override
    public boolean checkAccessToken() {
        boolean valid = false;

        if(this.credential == null) {
            this.credential = new GoogleCredential();
            this.credential.setAccessToken(this.accessToken);
        }

        Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                .setApplicationName("SlideshowFX")
                .build();

        try {
            service.about()
                    .get()
                    .execute();
            valid = true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not determine if access token is valid", e);
        }

        return valid;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws FileNotFoundException {
        if(this.isAuthenticated()) {
            Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();

            com.google.api.services.drive.model.File body = null;

            if(overwrite) {
                // Search for the file to get its ID and then delete it.
                StringBuilder query = new StringBuilder()
                        .append("mimeType != 'application/vnd.google-apps.folder'")
                        .append(" and not trashed")
                        .append(String.format(" and '%1$s' in parents", ((GoogleFile) folder).getId()))
                        .append(String.format(" and title = '%1$s'", engine.getArchive().getName()));

                try {
                    FileList files = service.files()
                            .list()
                            .setQ(query.toString())
                            .execute();

                    if(!files.getItems().isEmpty()) {
                        body = files.getItems().get(0);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not find file to overwrite", e);
                }
            } else {
                body = new com.google.api.services.drive.model.File();
                body.setMimeType(SLIDESHOWFX_MIME_TYPE);
                body.setParents(Arrays.asList(new ParentReference()
                                .setId(((GoogleFile) folder).getId())
                ));

                if(this.fileExists(engine, folder)) {
                    final String nameWithoutExtension = engine.getArchive().getName().substring(0, engine.getArchive().getName().lastIndexOf("."));
                    final Calendar calendar = Calendar.getInstance();

                    body.setTitle(String.format("%1$s %2$tF %2$tT.%3$s", nameWithoutExtension, calendar, engine.getArchiveExtension()));
                } else {
                    body.setTitle(engine.getArchive().getName());
                }
            }

            FileContent mediaContent = new FileContent(SLIDESHOWFX_MIME_TYPE, engine.getArchive());

            try {
                if(overwrite)
                    service.files()
                            .update(body.getId(), body, mediaContent)
                            .setNewRevision(true)
                            .execute();
                else service.files().insert(body, mediaContent).execute();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not upload presentation to Google Drive", e);
            }
        }

    }

    @Override
    public File download(File destination, RemoteFile file) {
        if(destination == null) throw new NullPointerException("The destination can not be null");
        if(file == null) throw new NullPointerException("The file to download can not be null");
        if(!destination.isDirectory()) throw new IllegalArgumentException("The destination is not a folder");
        if(!(file instanceof GoogleFile)) throw new IllegalArgumentException("The file is not a GoogleFile");

        File result = null;

        if(this.isAuthenticated()) {
            result = new File(destination, file.getName());

            Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();

            try(final OutputStream out = new FileOutputStream(result)) {
                final HttpResponse response = service.getRequestFactory()
                                                .buildGetRequest(new GenericUrl(((GoogleFile) file).getDownloadUrl()))
                                                .execute();
                response.download(out);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not download the file", e);
                result = null;
            }

        }

        return result;
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) {
        if(parent == null) throw new NullPointerException("The parent can not be null");
        if(!(parent instanceof GoogleFile)) throw new IllegalArgumentException("The given parent must be a GoogleFile");

        final List<RemoteFile> folders = new ArrayList<>();

        if(this.isAuthenticated()) {
            final Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();

            try {
                final StringBuilder query = new StringBuilder();

                if(includeFolders && includePresentations) query.append("(mimeType = 'application/vnd.google-apps.folder' or mimeType = '")
                                                        .append(SLIDESHOWFX_MIME_TYPE).append("') and ");
                if(includeFolders && !includePresentations) query.append("mimeType = 'application/vnd.google-apps.folder' and ");
                if(!includeFolders && includePresentations) query.append("mimeType = '").append(SLIDESHOWFX_MIME_TYPE).append("' and ");

                query.append("not trashed ")
                        .append("and '").append(((GoogleFile) parent).getId()).append("' in parents");

                FileList files = service.files()
                        .list()
                        .setQ(query.toString())
                        .execute();

                GoogleFile child;
                for(com.google.api.services.drive.model.File reference : files.getItems()) {
                    child = new GoogleFile((GoogleFile) parent, reference.getTitle(), reference.getId());
                    child.setDownloadUrl(reference.getDownloadUrl());
                    folders.add(child);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not list folders of Google Drive", e);
            }
        }

        return folders;
    }

    @Override
    public boolean fileExists(PresentationEngine engine, RemoteFile destination) {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if(destination == null) throw new NullPointerException("The destination can not be null");
        if(!(destination instanceof GoogleFile)) throw new IllegalArgumentException("The given destination must be a GoogleFile");

        boolean exist = true;

        if(this.isAuthenticated()) {
            final Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName("SlideshowFX")
                    .build();

            try {
                StringBuilder query = new StringBuilder()
                        .append("mimeType != 'application/vnd.google-apps.folder'")
                        .append(" and not trashed")
                        .append(String.format(" and '%1$s' in parents", ((GoogleFile) destination).getId()))
                        .append(String.format(" and title = '%1$s'", engine.getArchive().getName()));

                FileList files = service.files()
                        .list()
                        .setQ(query.toString())
                        .execute();

                exist = !files.getItems().isEmpty();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not list files of Google Drive", e);
            }
        }

        return exist;
    }
}
