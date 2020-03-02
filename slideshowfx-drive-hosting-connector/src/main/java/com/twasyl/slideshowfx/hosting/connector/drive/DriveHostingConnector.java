package com.twasyl.slideshowfx.hosting.connector.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.BasicHostingConnectorOptions;
import com.twasyl.slideshowfx.hosting.connector.drive.io.GoogleFile;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.plugin.Plugin;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.api.services.drive.DriveScopes.DRIVE;
import static java.util.Collections.singletonList;

/**
 * This connector allows to interact with Google Drive.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
@Plugin
public class DriveHostingConnector extends AbstractHostingConnector<BasicHostingConnectorOptions> {
    private static final Logger LOGGER = Logger.getLogger(DriveHostingConnector.class.getName());
    private static final String SLIDESHOWFX_MIME_TYPE = "application/slideshowfx";
    private static final String APPLICATION_NAME = "SlideshowFX";

    private Credential credential;

    public DriveHostingConnector() {
        super("googledrive", "Google Drive", new GoogleFile());

        this.setOptions(new BasicHostingConnectorOptions());

        String configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(CONSUMER_KEY_PROPERTY_SUFFIX));
        if (configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setConsumerKey(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(CONSUMER_SECRET_PROPERTY_SUFFIX));
        if (configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setConsumerSecret(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(REDIRECT_URI_PROPERTY_SUFFIX));
        if (configuration != null && !configuration.trim().isEmpty()) {
            this.getOptions().setRedirectUri(configuration.trim());
        }

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX));
        if (configuration != null && !configuration.trim().isEmpty()) {
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

        return new VBox(5, consumerKeyBox, consumerSecretBox, redirectUriBox);
    }

    @Override
    public void saveNewOptions() {
        if (this.getNewOptions() != null) {
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
        if (this.getOptions().getConsumerKey() == null || this.getOptions().getConsumerSecret() == null) {
            throw new HostingConnectorException(HostingConnectorException.MISSING_CONFIGURATION);
        }

        final HttpTransport httpTransport = new NetHttpTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                this.getOptions().getConsumerKey(),
                this.getOptions().getConsumerSecret(),
                singletonList(DRIVE))
                .setAccessType("online")
                .setApprovalPrompt("auto")
                .build();

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);
        browser.getEngine().locationProperty().addListener((locationProperty, oldLocation, newLocation) -> {
            if (newLocation != null && newLocation.startsWith(this.getOptions().getRedirectUri())) {

                try {
                    final Map<String, String> uriParameters = getURIParameters(new URI(newLocation));

                    if (uriParameters.containsKey("code")) {
                        final String authorizationCode = uriParameters.get("code");

                        final GoogleTokenResponse response = flow.newTokenRequest(authorizationCode)
                                .setRedirectUri(this.getOptions().getRedirectUri())
                                .execute();

                        this.credential = flow.createAndStoreCredential(response, this.getOptions().getConsumerKey());
                        this.accessToken = this.credential.getAccessToken();
                    }
                } catch (URISyntaxException e) {
                    LOGGER.log(Level.SEVERE, "Error when parsing the redirect URI", e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to get access token", e);
                    this.accessToken = null;
                } finally {
                    if (this.accessToken != null) {
                        GlobalConfiguration.setProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX),
                                this.accessToken);
                    }
                    stage.close();
                }
            }
        });
        browser.getEngine().load(flow.newAuthorizationUrl()
                .setRedirectUri(this.getOptions().getRedirectUri())
                .build());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Google Drive");
        stage.showAndWait();

        if (!this.isAuthenticated())
            throw new HostingConnectorException(HostingConnectorException.AUTHENTICATION_FAILURE);
    }

    @Override
    public boolean checkAccessToken() {
        boolean valid = false;

        if (this.credential == null) {
            this.credential = new GoogleCredential();
            this.credential.setAccessToken(this.accessToken);
        }

        Drive service = new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), this.credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        try {
            service.about()
                    .get()
                    .setFields("user, storageQuota")
                    .execute();
            valid = true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Can not determine if access token is valid", e);
        }

        return valid;
    }

    @Override
    public void disconnect() {
        // Nothing particular to do when disconnecting
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws HostingConnectorException, FileNotFoundException {
        if (this.isAuthenticated()) {
            Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            com.google.api.services.drive.model.File body;

            if (overwrite) {
                body = getFile(engine, folder);

                // We need to delete the file before updating
                if (body != null) {
                    try {
                        service.files().delete(body.getId()).execute();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Can not delete the existing file remotely", e);
                    }
                }

                body = this.buildFile(folder, engine);
            } else {
                body = this.buildFile(folder, engine);

                if (this.fileExists(engine, folder)) {
                    final String nameWithoutExtension = engine.getArchive().getName().substring(0, engine.getArchive().getName().lastIndexOf('.'));
                    final Calendar calendar = Calendar.getInstance();

                    body.setName(String.format("%1$s %2$tF %2$tT.%3$s", nameWithoutExtension, calendar, engine.getArchiveExtension()));
                }
            }

            final FileContent mediaContent = new FileContent(SLIDESHOWFX_MIME_TYPE, engine.getArchive());

            try {
                service.files().create(body, mediaContent).setFields("id").execute();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not upload presentation to Google Drive", e);
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }
    }

    @Override
    public File download(File destination, RemoteFile file) throws HostingConnectorException {
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (file == null) throw new NullPointerException("The file to download can not be null");
        if (!destination.isDirectory()) throw new IllegalArgumentException("The destination is not a folder");
        if (!(file instanceof GoogleFile)) throw new IllegalArgumentException("The file is not a GoogleFile");

        File result;

        if (this.isAuthenticated()) {
            result = new File(destination, file.getName());

            Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            try (final OutputStream out = new FileOutputStream(result)) {
                service.files().get(((GoogleFile) file).getId()).executeMediaAndDownloadTo(out);
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
        if (parent == null) throw new NullPointerException("The parent can not be null");
        if (!(parent instanceof GoogleFile))
            throw new IllegalArgumentException("The given parent must be a GoogleFile");

        final List<RemoteFile> folders = new ArrayList<>();

        if (this.isAuthenticated()) {
            final Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            try {
                final StringBuilder query = new StringBuilder();

                if (includeFolders && includePresentations)
                    query.append("(mimeType = 'application/vnd.google-apps.folder' or mimeType = '")
                            .append(SLIDESHOWFX_MIME_TYPE).append("') and ");
                if (includeFolders && !includePresentations)
                    query.append("mimeType = 'application/vnd.google-apps.folder' and ");
                if (!includeFolders && includePresentations)
                    query.append("mimeType = '").append(SLIDESHOWFX_MIME_TYPE).append("' and ");

                query.append("not trashed ")
                        .append("and '").append(((GoogleFile) parent).getId()).append("' in parents");

                final FileList files = service.files()
                        .list()
                        .setQ(query.toString())
                        .execute();

                GoogleFile child;
                for (com.google.api.services.drive.model.File reference : files.getFiles()) {
                    child = new GoogleFile((GoogleFile) parent, reference.getName(), reference.getId());
                    child.setDownloadUrl(reference.getWebContentLink());
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
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (!(destination instanceof GoogleFile))
            throw new IllegalArgumentException("The given destination must be a GoogleFile");

        return this.getRemoteFile(engine, destination) != null;
    }

    @Override
    public RemoteFile getRemoteFile(PresentationEngine engine, RemoteFile destination) throws HostingConnectorException {
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (!(destination instanceof GoogleFile))
            throw new IllegalArgumentException("The given destination must be a GoogleFile");

        RemoteFile remoteFile = null;

        if (this.isAuthenticated()) {
            final com.google.api.services.drive.model.File file = getFile(engine, destination);

            if (file != null) {
                remoteFile = new GoogleFile((GoogleFile) destination, file.getName(), file.getId());
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return remoteFile;
    }

    /**
     * Get the {@link com.google.api.services.drive.model.File file} present remotely in the provided folder.
     *
     * @param engine The presentation to find remotely.
     * @param folder The folder in which the search will be performed.
     * @return The corresponding {@link com.google.api.services.drive.model.File} to the presentation or {@code null} if not found.
     */
    protected com.google.api.services.drive.model.File getFile(final PresentationEngine engine, final RemoteFile folder) {
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if (folder == null) throw new NullPointerException("The folder can not be null");
        if (!(folder instanceof GoogleFile))
            throw new IllegalArgumentException("The given folder must be a GoogleFile");

        com.google.api.services.drive.model.File result = null;

        final Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), this.credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        try {
            final StringBuilder query = new StringBuilder()
                    .append("mimeType != 'application/vnd.google-apps.folder'")
                    .append(" and not trashed")
                    .append(String.format(" and '%1$s' in parents", ((GoogleFile) folder).getId()))
                    .append(String.format(" and name = '%1$s'", engine.getArchive().getName()));

            final FileList files = service.files()
                    .list()
                    .setFields("nextPageToken, files(id, name)")
                    .setSpaces("drive")
                    .setQ(query.toString())
                    .execute();

            if (!files.getFiles().isEmpty()) {
                result = files.getFiles().get(0);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not list files of Google Drive", e);
        }

        return result;
    }

    /**
     * Builds an instance of {@link com.google.api.services.drive.model.File} and initialized correctly with SlideshowFX
     * information.
     *
     * @param destination The location where the file will be created.
     * @param engine
     * @return A well constructed instance of {@link com.google.api.services.drive.model.File}.
     */
    protected com.google.api.services.drive.model.File buildFile(RemoteFile destination, PresentationEngine engine) {
        com.google.api.services.drive.model.File body;
        body = new com.google.api.services.drive.model.File();
        body.setMimeType(SLIDESHOWFX_MIME_TYPE);

        if (destination instanceof GoogleFile) {
            final GoogleFile googleFolder = (GoogleFile) destination;

            body.setParents(singletonList(googleFolder.getId()));
        }

        body.setName(engine.getArchive().getName());
        return body;
    }
}
