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

package com.twasyl.slideshowfx.hosting.connector;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * This interface defines an hosting connector that will enable to connect and interact with a file hosting provider.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface IHostingConnector {

    /**
     * Returns the code that uniquely identifies this connector. The code must not contain spaces.
     * @return The code of this connector.
     */
    String getCode();

    /**
     * The name of the connector that will be used to identify it in the UI.
     * @return The name of the connector.
     */
    String getName();

    /**
     * Indicates if the user is authenticated to the cloud based platform.
     * @return {@code true} if the user is authenticated, {@code false} otherwise.
     */
    boolean isAuthenticated();

    /**
     * Authenticate the user to the cloud based platform.
     * @return {@code true} if the user has been authenticated, {@code false} otherwise.
     */
    boolean authenticate();

    /**
     * Get the access token that is get when the user is successfully logged in the service.
     * @return The access token get by the authentication process.
     */
    String getAccessToken();

    /**
     * Checks if the current access token is valid or not.
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
     * @param engine The presentation to upload.
     * @throws java.lang.NullPointerException If the {@code engine} is {@code null} or if {@code engine.getArchive()}
     *                                          is {@code null}.
     * @throws java.io.FileNotFoundException If the archive file does not already exist.
     */
    void upload(PresentationEngine engine) throws FileNotFoundException;

    /**
     * Upload the given {@code engine} to the service. The presentation is uploaded in the given {@code folder}.
     * @param engine The presentation to upload.
     * @param folder The folder where the presented will be uploaded.
     * @param overwrite Indicates if it should overwrite the file if it already exists.
     * @throws java.lang.NullPointerException If the {@code engine} is {@code null} or if {@code engine.getArchive()}
     *                                          is {@code null}.
     * @throws java.io.FileNotFoundException If the archive file does not already exist.
     */
    void upload(PresentationEngine engine, RemoteFile folder, boolean overwrite) throws FileNotFoundException;

    /**
     * Download a presentation located by {@code file} in the {@code destination} folder.
     * @param destination The folder where the presentation should be saved.
     * @param file The presentation to download
     * @return The File object representing the downloaded presentation.
     * @throws java.lang.NullPointerException If either {@code destination} or {@code file} is null
     * @throws java.lang.IllegalArgumentException If {@code destination} is not a folder.
     */
    File download(File destination, RemoteFile file);

    /**
     * Returns the root folder of the service.
     * @return The root folder of the service.
     */
    RemoteFile getRootFolder();

    /**
     * List all content already present in the {@code parent} directory.
     * If {@code includeFolders == true}, folders will be included in the final list.
     * If {@code includePresentations == true}, presentation files will be included in the final list.
     * @param parent The folder to list the content.
     * @param includeFolders {@code true} to list the folders in {@code parent}
     * @param includePresentations {@code true} to list the presentations in {@code parent}
     * @return The list of all content present remotely in the parent.
     * @throws java.lang.NullPointerException If {@code parent} is null.
     */
    List<RemoteFile> list(RemoteFile parent, boolean includeFolders, boolean includePresentations);

    /**
     * Shows a dialog allowing the user to choose a file available on the hosting service.
     * The root folder is obtain ed by {@link IHostingConnector#getRootFolder()}.
     * Each time a folder is opened in the dialog, this method calls
     * {@link IHostingConnector#list(com.twasyl.slideshowfx.hosting.connector.io.RemoteFile, boolean, boolean)}
     * with the opened file as parent and {@code showFolders} and {@code showFiles} as arguments.
     * If the dialog is cancelled or if no selection is done, {@code null} will be returned.
     *
     * @param showFolders {@code true} for showing the folders.
     * @param showFiles {@code true} for showing the files.
     * @return The selected destination or {@code null} if the dialog is cancelled or if no selection is performed.
     */
    RemoteFile chooseFile(boolean showFolders, boolean showFiles);

    /**
     * Tests if the file of the {@code engine} exists in the {@code destination} folder present remotely.
     * @param engine The presentation to test the existence remotely.
     * @param destination The folder where the presentation should be uploaded. The test will be performed in this folder.
     * @return {@code true} if the file already exists in the {@code destination} folder, {@code false otherwise}.
     *
     * @throws java.lang.NullPointerException If either {@code engine},
     * {@link com.twasyl.slideshowfx.engine.presentation.PresentationEngine#getArchive()} or {@code destination} is null.
     */
    boolean fileExists(PresentationEngine engine, RemoteFile destination);
}
