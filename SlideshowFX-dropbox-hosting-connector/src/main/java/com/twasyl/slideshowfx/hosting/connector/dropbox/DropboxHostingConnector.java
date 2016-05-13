package com.twasyl.slideshowfx.hosting.connector.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
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
import org.w3c.dom.NamedNodeMap;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.engine.presentation.PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION;

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
                final Element authCode = browser.getEngine().getDocument().getElementById("auth-code");

                if (authCode != null && authCode.hasChildNodes()) {
                    final NamedNodeMap attributes = authCode.getFirstChild().getAttributes();
                    String dataToken = null;

                    if (attributes != null && (dataToken = attributes.getNamedItem("data-token").getTextContent()) != null) {
                        try {
                            final DbxAuthFinish authenticationFinish = authentication.finish(dataToken);
                            this.accessToken = authenticationFinish.getAccessToken();
                        } catch (DbxException e) {
                            LOGGER.log(Level.SEVERE, "Can not finish authentication", e);
                            this.accessToken = null;
                        } finally {
                            if (this.accessToken != null) {
                                GlobalConfiguration.setProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX), this.accessToken);
                            }
                            stage.close();
                        }
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

        final DbxClientV2 client = new DbxClientV2(this.dropboxConfiguration, this.accessToken);
        try {
            client.users().getCurrentAccount();
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
            final String computedName = folder.toString().concat("/".concat(engine.getArchive().getName()));
            final DbxClientV2 client = new DbxClientV2(this.dropboxConfiguration, this.accessToken);

            WriteMode writeMode;
            final StringBuilder fileName = new StringBuilder();

            final UploadBuilder uploader = client.files().uploadBuilder(computedName);

            if(overwrite) {
                try {
                    final Metadata metadata = client.files().getMetadata(computedName);

                    // Ensure the file has been found.
                    if(metadata != null) {
                        writeMode = WriteMode.OVERWRITE;
                        uploader.withAutorename(true);
                    } else {
                        writeMode = WriteMode.ADD;
                    }
                } catch (DbxException e) {
                    LOGGER.log(Level.SEVERE, "Can not get file metadata");
                    writeMode = WriteMode.ADD;
                }
            } else {
                writeMode = WriteMode.ADD;
                uploader.withAutorename(this.fileExists(engine, folder));
            }

            uploader.withMode(writeMode);

            try(final InputStream archiveStream = new FileInputStream(engine.getArchive())) {
                uploader.start().uploadAndFinish(archiveStream);
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

        File result;

        if(this.isAuthenticated()) {
            result = new File(destination, file.getName());

            try(final OutputStream out = new FileOutputStream(result)) {
                final DbxClientV2 client = new DbxClientV2(this.dropboxConfiguration, this.accessToken);

                client.files().download(file.toString()).download(out);
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
            final DbxClientV2 client = new DbxClientV2(this.dropboxConfiguration, this.accessToken);
            final ListFolderResult listing;

            try {
                listing = client.files().listFolderBuilder(parent.isRoot() ? "" : parent.toString())
                                        .withRecursive(false)
                                        .withIncludeDeleted(false)
                                        .start();
                listing.getEntries()
                        .stream()
                        .filter(entry -> {
                            if(includeFolders && includePresentations) {
                                return isFolder(entry) || (isFile(entry) && isNameEndingWithSuffix(entry, DEFAULT_DOTTED_ARCHIVE_EXTENSION));
                            } else if(includeFolders && !includePresentations) {
                                return isFolder(entry);
                            } else if(!includeFolders && includePresentations) {
                                return isFolder(entry) && isNameEndingWithSuffix(entry, DEFAULT_DOTTED_ARCHIVE_EXTENSION);
                            } else return false;
                        })
                        .forEach(entry ->  folders.add(this.createRemoteFile(entry, parent)));
            } catch (DbxException e) {
                LOGGER.log(Level.SEVERE, "Error while retrieving the folders", e);
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return folders;
    }

    /**
     * Creates an instance of {@link RemoteFile} from a given {@link Metadata} and a given parent.
     * @param metadata The metadata to create the remote file for.
     * @param parent The optional parent of the file.
     * @return A well created {@link RemoteFile} instance.
     */
    protected RemoteFile createRemoteFile(final Metadata metadata, final RemoteFile parent) {
        final RemoteFile file = new RemoteFile(parent, metadata.getName());
        if(isFile(metadata)) {
            file.setFile(true);
            file.setFolder(false);
        } else {
            file.setFile(false);
            file.setFolder(true);
        }

        return file;
    }

    @Override
    public boolean fileExists(PresentationEngine engine, RemoteFile destination) throws HostingConnectorException {
        if(engine == null) throw new NullPointerException("The engine can not be null");
        if(engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if(destination == null) throw new NullPointerException("The destination can not be null");

        boolean exist;

        if(this.isAuthenticated()) {
            final DbxClientV2 client = new DbxClientV2(this.dropboxConfiguration, this.accessToken);
            final RemoteFile remotePresentation = new RemoteFile(destination, engine.getArchive().getName());

            try {
                client.files().getMetadata(remotePresentation.toString());
                exist = true;
            } catch (DbxException e) {
                LOGGER.log(Level.FINE, "The presentation hasn't been found remotely", e);
                exist = false;
            }
        } else {
            throw new HostingConnectorException(HostingConnectorException.NOT_AUTHENTICATED);
        }

        return exist;
    }

    /**
     * Check if a given metadata is considered as a folder or not.
     * @param metadata The metadata to check.
     * @return {@code true} if the metadata is a folder, {@code false} otherwise.
     */
    protected boolean isFolder(final Metadata metadata) {
        return metadata instanceof FolderMetadata;
    }

    /**
     * Check if a given metadata is considered as a file or not.
     * @param metadata The metadata to check.
     * @return {@code true} if the metadata is a file, {@code false} otherwise.
     */
    protected boolean isFile(final Metadata metadata) {
        return metadata instanceof FileMetadata;
    }

    /**
     * Check if the name of a metada is ending with a given suffix.
     * @param metadata The metadata to check the name for.
     * @param suffix The suffix expected at the end of the metadata's name.
     * @return {@code true} if the metadata is ending with the suffix, {@code false} otherwise.
     */
    protected boolean isNameEndingWithSuffix(final Metadata metadata, final String suffix) {
        return metadata.getName().endsWith(suffix);
    }

    /**
     * Check if the name of the metadata is equal to another name. The check is case sensitive.
     * @param metadata The metadata to check the name.
     * @param name The expected name to be considered equal.
     * @return {@code true} if the names are equal, {@code false} otherwise.
     */
    protected boolean isNameEqual(final Metadata metadata, final String name) {
        return metadata.getName().equals(name);
    }
}
