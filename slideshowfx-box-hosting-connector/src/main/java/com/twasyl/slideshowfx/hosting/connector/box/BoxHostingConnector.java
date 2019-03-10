package com.twasyl.slideshowfx.hosting.connector.box;

import com.box.sdk.*;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.AbstractHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.BasicHostingConnectorOptions;
import com.twasyl.slideshowfx.hosting.connector.box.io.BoxFile;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.engine.presentation.PresentationEngine.DEFAULT_DOTTED_ARCHIVE_EXTENSION;
import static com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException.NOT_AUTHENTICATED;

/**
 * This connector allows to interact with Box.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.1
 */
public class BoxHostingConnector extends AbstractHostingConnector<BasicHostingConnectorOptions> {
    private static final Logger LOGGER = Logger.getLogger(BoxHostingConnector.class.getName());

    protected static final String REFRESH_TOKEN_PROPERTY_SUFFIX = ".refreshtoken";

    private BoxAPIConnection boxApi;
    private String refreshToken;

    public BoxHostingConnector() {
        super("box", "Box", new BoxFile());

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

        configuration = GlobalConfiguration.getProperty(getConfigurationBaseName().concat(REFRESH_TOKEN_PROPERTY_SUFFIX));
        if (configuration != null && !configuration.trim().isEmpty()) {
            this.refreshToken = configuration;
        }

        if (this.getOptions().getConsumerKey() != null && this.getOptions().getConsumerSecret() != null) {
            this.boxApi = new BoxAPIConnection(this.getOptions().getConsumerKey(), this.getOptions().getConsumerSecret());

            if (this.accessToken != null && !this.accessToken.isEmpty()) {
                this.boxApi.setAccessToken(this.accessToken);
            }

            if (this.refreshToken != null && !this.refreshToken.isEmpty()) {
                this.boxApi.setRefreshToken(this.refreshToken);
            }
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

            if (this.getOptions().getConsumerKey() != null && this.getOptions().getConsumerSecret() != null) {
                this.boxApi = new BoxAPIConnection(this.getOptions().getConsumerKey(), this.getOptions().getConsumerSecret());
            }
        }
    }

    @Override
    public void authenticate() throws HostingConnectorException {
        if (this.boxApi == null) throw new HostingConnectorException(HostingConnectorException.MISSING_CONFIGURATION);

        final WebView browser = new WebView();
        final Scene scene = new Scene(browser);
        final Stage stage = new Stage();

        browser.setPrefSize(500, 500);

        browser.getEngine().locationProperty().addListener((locationProperty, oldLocation, newLocation) -> {
            if (newLocation != null && newLocation.startsWith(this.getOptions().getRedirectUri())) {
                try {
                    final Map<String, String> uriParameters = getURIParameters(new URI(newLocation));

                    if (uriParameters.containsKey("code")) {
                        this.boxApi.authenticate(uriParameters.get("code"));
                        this.accessToken = this.boxApi.getAccessToken();
                        this.refreshToken = this.boxApi.getRefreshToken();
                    }
                } catch (URISyntaxException e) {
                    LOGGER.log(Level.SEVERE, "Error when parsing the redirect URI", e);
                } finally {
                    if (this.accessToken != null) {
                        GlobalConfiguration.setProperty(getConfigurationBaseName().concat(ACCESS_TOKEN_PROPERTY_SUFFIX), this.accessToken);
                    }
                    if (this.refreshToken != null) {
                        GlobalConfiguration.setProperty(getConfigurationBaseName().concat(REFRESH_TOKEN_PROPERTY_SUFFIX), this.refreshToken);
                    }
                    stage.close();
                }
            }
        });

        browser.getEngine().load(getAuthenticationURL());

        stage.setScene(scene);
        stage.setTitle("Authorize SlideshowFX in Box");
        stage.showAndWait();

        if (!this.isAuthenticated())
            throw new HostingConnectorException(HostingConnectorException.AUTHENTICATION_FAILURE);
    }

    /**
     * Get the URL allowing to ask the user the authorization for SlideshowFX to Box.
     *
     * @return The authorization URL.
     */
    protected String getAuthenticationURL() {
        final StringBuilder url = new StringBuilder("https://account.box.com/api/oauth2/authorize")
                .append("?response_type=code")
                .append("&client_id=").append(this.getOptions().getConsumerKey())
                .append("&redirect_uri=").append(this.getOptions().getRedirectUri())
                .append("&state=").append(System.currentTimeMillis());

        return url.toString();
    }

    @Override
    public boolean checkAccessToken() {
        boolean valid = false;

        if (this.boxApi != null) {
            try {
                BoxUser.getCurrentUser(this.boxApi);
                valid = true;
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error when trying to check the access token", e);
            }
        }

        return valid;
    }

    @Override
    public void disconnect() {
        // Nothing particular to do when disconnecting
    }

    @Override
    public void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws HostingConnectorException, FileNotFoundException {
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive to upload can not be null");
        if (!engine.getArchive().exists()) throw new FileNotFoundException("The archive to upload does not exist");
        if (!(folder instanceof BoxFile)) throw new IllegalArgumentException("The given folder must be a BoxFile");

        if (this.isAuthenticated()) {
            final BoxFolder destination = folder.isRoot() ? BoxFolder.getRootFolder(this.boxApi) : new BoxFolder(this.boxApi, ((BoxFile) folder).getId());

            final FileUploadParams parameters = new FileUploadParams();
            final BoxFile existingFile = (BoxFile) getRemoteFile(engine, folder);
            final boolean fileAlreadyExists = existingFile != null;

            if (overwrite && fileAlreadyExists) {
                final com.box.sdk.BoxFile remoteFile = new com.box.sdk.BoxFile(this.boxApi, existingFile.getId());
                remoteFile.delete();

                parameters.setName(engine.getArchive().getName());
            } else if (!overwrite && fileAlreadyExists) {
                final String nameWithoutExtension = engine.getArchive().getName().substring(0, engine.getArchive().getName().lastIndexOf('.'));
                final Calendar calendar = Calendar.getInstance();

                parameters.setName(String.format("%1$s %2$tF %2$tT.%3$s", nameWithoutExtension, calendar, engine.getArchiveExtension()));
            } else {
                parameters.setName(engine.getArchive().getName());
            }

            parameters.setSize(engine.getArchive().length());
            parameters.setModified(new Date(System.currentTimeMillis()));

            try (final FileInputStream input = new FileInputStream(engine.getArchive())) {
                parameters.setContent(input);

                destination.uploadFile(parameters);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not upload the presentation", e);
            }
        } else {
            throw new HostingConnectorException(NOT_AUTHENTICATED);
        }
    }

    @Override
    public File download(File destination, RemoteFile file) throws HostingConnectorException {
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (file == null) throw new NullPointerException("The file to download can not be null");
        if (!(file instanceof BoxFile)) throw new IllegalArgumentException("The given file must be a BoxFile");
        if (!destination.isDirectory()) throw new IllegalArgumentException("The destination is not a folder");

        File result;

        if (this.isAuthenticated()) {
            final com.box.sdk.BoxFile fileToDownload = new com.box.sdk.BoxFile(this.boxApi, ((BoxFile) file).getId());
            result = new File(destination, file.getName());

            try (final FileOutputStream output = new FileOutputStream(result)) {
                fileToDownload.download(output);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not download the file", e);
                result = null;
            }
        } else {
            throw new HostingConnectorException(NOT_AUTHENTICATED);
        }

        return result;
    }

    @Override
    public List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) throws HostingConnectorException {
        if (parent == null) throw new NullPointerException("The parent can not be null");
        if (!(parent instanceof BoxFile)) throw new IllegalArgumentException("The given parent must be a BoxFile");

        final List<RemoteFile> folders = new ArrayList<>();

        if (this.isAuthenticated()) {
            final BoxFolder folder = parent.isRoot() ? BoxFolder.getRootFolder(this.boxApi)
                    : new BoxFolder(this.boxApi, ((BoxFile) parent).getId());

            folder.forEach(child -> {
                if (canFileBeLister(child, includeFolders, includePresentations)) {
                    folders.add(this.createRemoteFile(child, (BoxFile) parent));
                }
            });
        } else {
            throw new HostingConnectorException(NOT_AUTHENTICATED);
        }

        return folders;
    }

    @Override
    public boolean fileExists(PresentationEngine engine, RemoteFile destination) throws HostingConnectorException {
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (!(destination instanceof BoxFile))
            throw new IllegalArgumentException("The given destination must be a BoxFile");

        return getRemoteFile(engine, destination) != null;
    }

    /**
     * Creates an instance of {@link RemoteFile} from a given {@link BoxItem.Info} and a given parent.
     *
     * @param info   The info to create the remote file for.
     * @param parent The optional parent of the file.
     * @return A well created {@link RemoteFile} instance.
     */
    protected RemoteFile createRemoteFile(final BoxItem.Info info, final BoxFile parent) {
        final BoxFile file = new BoxFile(parent, info.getName(), info.getID());

        if (isFile(info)) {
            file.setFile(true);
            file.setFolder(false);
        } else {
            file.setFile(false);
            file.setFolder(true);
        }

        return file;
    }

    @Override
    public RemoteFile getRemoteFile(final PresentationEngine engine, final RemoteFile destination) throws HostingConnectorException {
        if (engine == null) throw new NullPointerException("The engine can not be null");
        if (engine.getArchive() == null) throw new NullPointerException("The archive file can not be null");
        if (destination == null) throw new NullPointerException("The destination can not be null");
        if (!(destination instanceof BoxFile))
            throw new IllegalArgumentException("The given destination must be a BoxFile");

        RemoteFile remoteFile = null;

        if (this.isAuthenticated()) {
            final BoxFolder folder = destination.isRoot() ? BoxFolder.getRootFolder(this.boxApi)
                    : new BoxFolder(this.boxApi, ((BoxFile) destination).getId());

            final Iterator<BoxItem.Info> children = folder.iterator();
            boolean found = false;

            while (!found && children.hasNext()) {
                final BoxItem.Info child = children.next();
                found = isFile(child) && isNameEqual(child, engine.getArchive().getName());

                if (found) {
                    remoteFile = new BoxFile((BoxFile) destination, engine.getArchive().getName(), child.getID());
                }
            }
        } else {
            throw new HostingConnectorException(NOT_AUTHENTICATED);
        }

        return remoteFile;
    }

    /**
     * Check if a given {@link BoxItem.Info} can be listed in the UI.
     *
     * @param child                The info to check.
     * @param includeFolders       Indicates if the folders are allowed to be listed.
     * @param includePresentations Indicates if the presentations are allowed to be listed.
     * @return {@code true} if the child can be listed, {@code false} otherwise.
     */
    protected boolean canFileBeLister(BoxItem.Info child, boolean includeFolders, boolean includePresentations) {
        boolean canBeListed = false;

        if (includeFolders && includePresentations) {
            canBeListed = isFolder(child) || (isFile(child) && isNameEndingWithSuffix(child, DEFAULT_DOTTED_ARCHIVE_EXTENSION));
        } else if (includeFolders) {
            canBeListed = isFolder(child);
        } else if (includePresentations) {
            canBeListed = isFolder(child) && isNameEndingWithSuffix(child, DEFAULT_DOTTED_ARCHIVE_EXTENSION);
        }

        return canBeListed;
    }

    /**
     * Check if a given info is considered as a folder or not.
     *
     * @param info The info to check.
     * @return {@code true} if the info is a folder, {@code false} otherwise.
     */
    protected boolean isFolder(final BoxItem.Info info) {
        return info instanceof BoxFolder.Info;
    }

    /**
     * Check if a given info is considered as a file or not.
     *
     * @param info The info to check.
     * @return {@code true} if the info is a file, {@code false} otherwise.
     */
    protected boolean isFile(final BoxItem.Info info) {
        return info instanceof com.box.sdk.BoxFile.Info;
    }

    /**
     * Check if the name of an info is ending with a given suffix.
     *
     * @param info   The info to check the name for.
     * @param suffix The suffix expected at the end of the info's name.
     * @return {@code true} if the info is ending with the suffix, {@code false} otherwise.
     */
    protected boolean isNameEndingWithSuffix(final BoxItem.Info info, final String suffix) {
        return info.getName().endsWith(suffix);
    }

    /**
     * Check if the name of the info is equal to another name. The check is case sensitive.
     *
     * @param info The info to check the name.
     * @param name The expected name to be considered equal.
     * @return {@code true} if the names are equal, {@code false} otherwise.
     */
    protected boolean isNameEqual(final BoxItem.Info info, final String name) {
        return info.getName().equalsIgnoreCase(name);
    }
}
