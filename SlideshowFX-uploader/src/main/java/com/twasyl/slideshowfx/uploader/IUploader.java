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

package com.twasyl.slideshowfx.uploader;

import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.uploader.io.RemoteFile;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * This interface defines an uploader that will enable to upload a presentation to a cloud based platform.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public interface IUploader {

    /**
     * Returns the code that uniquely identifies this uploader. The code must not contain spaces.
     * @return The code of this uploader.
     */
    String getCode();

    /**
     * The name of the uploader that will be used to identify it in the UI.
     * @return The name of the uploader.
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
     * Returns the root folder of the service.
     * @return The root folder of the service.
     */
    RemoteFile getRootFolder();

    /**
     * List all folders already present in the {@code parent} directory.
     * @return The list of all folders present remotely in the parent.
     * @throws java.lang.NullPointerException If {@code parent} is null.
     */
    List<RemoteFile> getSubfolders(RemoteFile parent);

    /**
     * Shows a dialog allowing the user to choose the destination for its upload.
     * The root folder is obainted by {@link IUploader#getRootFolder()}.
     * Each time a folder is opened in the dialog, this method calls
     * {@link com.twasyl.slideshowfx.uploader.IUploader#getSubfolders(com.twasyl.slideshowfx.uploader.io.RemoteFile)} with the opened file as parent.
     * If the dialog is cancelled or if no selection is done, {@code null} will be returned.
     *
     * @return The selected destination or {@code null} if the dialog is cancelled or if no selection is performed.
     */
    RemoteFile chooseDestinationFile();

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
