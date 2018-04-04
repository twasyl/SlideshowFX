package com.twasyl.slideshowfx.hosting.connector;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.exceptions.HostingConnectorException;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;
import com.twasyl.slideshowfx.plugin.IConfigurable;
import com.twasyl.slideshowfx.plugin.IPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * This interface defines an hosting connector that will enable to connect and interact with a file hosting provider.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public interface IHostingConnector<T extends IHostingConnectorOptions> extends IPlugin<T>, IConfigurable<T> {

    /**
     * Returns the code that uniquely identifies this connector. The code must not contain spaces.
     *
     * @return The code of this connector.
     */
    String getCode();

    /**
     * Indicates if the user is authenticated to the cloud based platform.
     *
     * @return {@code true} if the user is authenticated, {@code false} otherwise.
     */
    boolean isAuthenticated();

    /**
     * Authenticate the user to the cloud based platform.
     *
     * @throws HostingConnectorException If the authentication process fails.
     */
    void authenticate() throws HostingConnectorException;

    /**
     * Get the access token that is get when the user is successfully logged in the service.
     *
     * @return The access token get by the authentication process.
     */
    String getAccessToken();

    /**
     * Checks if the current access token is valid or not.
     *
     * @return {@code true} is the access token is valid, {@code false} otherwise.
     */
    boolean checkAccessToken();

    /**
     * Disconnect the user from the service.
     */
    void disconnect();

    /**
     * Upload the given {@code engine} to the service. The presentation is uploaded to the root of the service. The
     * implementation should not overwrite the already existing file if any.
     *
     * @param engine The presentation to upload.
     * @throws NullPointerException If the {@code engine} is {@code null} or if {@code engine.getArchive()}
     *                                        is {@code null}.
     * @throws FileNotFoundException  If the archive file does not already exist.
     * @throws HostingConnectorException      If something went wrong due to configuration, authentication or an unknown error.
     */
    void upload(PresentationEngine engine) throws HostingConnectorException, FileNotFoundException;

    /**
     * Upload the given {@code engine} to the service. The presentation is uploaded in the given {@code folder}.
     *
     * @param engine    The presentation to upload.
     * @param folder    The folder where the presented will be uploaded.
     * @param overwrite Indicates if it should overwrite the file if it already exists.
     * @throws NullPointerException If the {@code engine} is {@code null} or if {@code engine.getArchive()}
     *                                        is {@code null}.
     * @throws FileNotFoundException  If the archive file does not already exist.
     * @throws HostingConnectorException      If something went wrong due to configuration, authentication or an unknown error.
     */
    void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws HostingConnectorException, FileNotFoundException;

    /**
     * Download a presentation located by {@code file} in the {@code destination} folder.
     *
     * @param destination The folder where the presentation should be saved.
     * @param file        The presentation to download
     * @return The File object representing the downloaded presentation.
     * @throws NullPointerException     If either {@code destination} or {@code file} is null
     * @throws IllegalArgumentException If {@code destination} is not a folder.
     * @throws HostingConnectorException          If something went wrong due to configuration, authentication or an unknown error.
     */
    File download(File destination, RemoteFile file) throws HostingConnectorException;

    /**
     * Returns the root folder of the service.
     *
     * @return The root folder of the service.
     * @throws HostingConnectorException If something went wrong due to configuration, authentication or an unknown error.
     */
    RemoteFile getRootFolder() throws HostingConnectorException;

    /**
     * List all content already present in the {@code parent} directory.
     * If {@code includeFolders == true}, folders will be included in the final list.
     * If {@code includePresentations == true}, presentation files will be included in the final list.
     *
     * @param parent               The folder to list the content.
     * @param includeFolders       {@code true} to list the folders in {@code parent}
     * @param includePresentations {@code true} to list the presentations in {@code parent}
     * @return The list of all content present remotely in the parent.
     * @throws NullPointerException If {@code parent} is null.
     * @throws HostingConnectorException      If something went wrong due to configuration, authentication or an unknown error.
     */
    List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations) throws HostingConnectorException;

    /**
     * Shows a dialog allowing the user to choose a file available on the hosting service.
     * The root folder is obtain ed by {@link IHostingConnector#getRootFolder()}.
     * Each time a folder is opened in the dialog, this method calls
     * {@link IHostingConnector#list(RemoteFile, boolean, boolean)}
     * with the opened file as parent and {@code showFolders} and {@code showFiles} as arguments.
     * If the dialog is cancelled or if no selection is done, {@code null} will be returned.
     *
     * @param showFolders {@code true} for showing the folders.
     * @param showFiles   {@code true} for showing the files.
     * @return The selected destination or {@code null} if the dialog is cancelled or if no selection is performed.
     * @throws HostingConnectorException If something went wrong due to configuration, authentication or an unknown error.
     */
    RemoteFile chooseFile(boolean showFolders, boolean showFiles) throws HostingConnectorException;

    /**
     * Tests if the file of the {@code engine} exists in the {@code destination} folder present remotely.
     *
     * @param engine      The presentation to test the existence remotely.
     * @param destination The folder where the presentation should be uploaded. The test will be performed in this folder.
     * @return {@code true} if the file already exists in the {@code destination} folder, {@code false otherwise}.
     * @throws NullPointerException If either {@code engine},
     *                                        {@link PresentationEngine#getArchive()} or {@code destination} is null.
     * @throws HostingConnectorException      If something went wrong due to configuration, authentication or an unknown error.
     */
    boolean fileExists(PresentationEngine engine, RemoteFile destination) throws HostingConnectorException;

    /**
     * Gets the {@link RemoteFile remote file} stored in the {@code destination} folder present remotely.
     *
     * @param engine      The presentation to get remotely.
     * @param destination The folder that should contain the file.
     * @return The remote file if it is found, {@code null} if not found.
     * @throws HostingConnectorException If something went wrong during the retrieval.
     */
    RemoteFile getRemoteFile(final PresentationEngine engine, final RemoteFile destination) throws HostingConnectorException;
}
