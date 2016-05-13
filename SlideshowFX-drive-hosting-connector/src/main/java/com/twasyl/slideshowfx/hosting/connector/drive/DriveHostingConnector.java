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
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.BasicHostingConnectorOptions;
import com.twasyl.slideshowfx.hosting.connector.drive.io.GoogleFile;
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
public class DriveHostingConnector extends AbstractHostingConnector<BasicHostingConnectorOptions> {
    private static final Logger LOGGER = Logger.getLogger(DriveHostingConnector.class.getName());
    private static final String SLIDESHOWFX_MIME_TYPE = "application/slideshowfx";

    private GoogleCredential credential;

    public DriveHostingConnector() {
        super("googledrive", "Google Drive", new GoogleFile());

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
        }
    }

    @Override
    public void authenticate() throws HostingConnectorException {
        if(this.getOptions().getConsumerKey() == null || this.getOptions().getConsumerSecret() == null) {
            throw new HostingConnectorException(HostingConnectorException.MISSING_CONFIGURATION);
        }

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = new JacksonFactory();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                this.getOptions().getConsumerKey(),
                this.getOptions().getConsumerSecret(),
                Arrays.asList(DriveScopes.DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto")
                .build();

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);
        browser.getEngine().getLoadWorker().stateProperty().addListener((stateValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {

                final HTMLInputElement codeElement = (HTMLInputElement) browser.getEngine().getDocument().getElementById("code");
                if (codeElement != null) {
                    final String authorizationCode = codeElement.getValue();

                    try {
                        final GoogleTokenResponse response = flow.newTokenRequest(authorizationCode.toString())
                                .setRedirectUri(this.getOptions().getRedirectUri())
                                .execute();

                        this.credential = new GoogleCredential().setFromTokenResponse(response);
                        this.accessToken = this.credential.getAccessToken();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to get access token", e);
                        this.accessToken = null;
                    } finally {
                        if(this.accessToken != null) {
                            GlobalConfiguration.setProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX),
                                    this.accessToken);
                        }
                        stage.close();
                    }

                }
            }
        });
        browser.getEngine().load(flow.newAuthorizationUrl()
                .setRedirectUri(this.getOptions().getRedirectUri())
                .build());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Google Drive");
        stage.showAndWait();

        if(!this.isAuthenticated()) throw new HostingConnectorException(HostingConnectorException.AUTHENTICATION_FAILURE);
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
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws HostingConnectorException, FileNotFoundException {
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

                if(folder instanceof GoogleFile) {
                    final GoogleFile googleFolder = (GoogleFile) folder;

                    final ParentReference parent = new ParentReference();
                    parent.setId(googleFolder.getId());

                    body.setParents(Arrays.asList(parent));
                }

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
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }
    }

    @Override
    public File download(File destination, RemoteFile file) throws HostingConnectorException {
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

        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return result;
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) throws HostingConnectorException {
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
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return exist;
    }
}
