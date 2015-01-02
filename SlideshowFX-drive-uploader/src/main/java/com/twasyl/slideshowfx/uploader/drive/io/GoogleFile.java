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

package com.twasyl.slideshowfx.uploader.drive.io;

import com.twasyl.slideshowfx.uploader.io.RemoteFile;

/**
 * An specific implementation of a {@link com.twasyl.slideshowfx.uploader.io.RemoteFile} for the representation of a file
 * on Google Drive. A {@link com.twasyl.slideshowfx.uploader.drive.io.GoogleFile} is composed of a parent (also a
 * GoogleFile), an ID and a name.
 * If a GoogleFile doesn't have a parent, it is considered at the root of Google Drive.
 * The ID of a GoogleFile is the one provided by Google and should not be created other than getting the real one on Drive.
 *
 * @author Thierry Wasylcznko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class GoogleFile extends RemoteFile {

    private String id;
    private boolean directory = false;
    private GoogleFile parent = null;

    /**
     * Constructs a GoogleFile according the given parameters. The {@code name} corresponds to the name of the file on
     * Google Drive and the {@code id} to the ID that identifies the file on Google Drive.
     * @param parent The parent of this Google File.
     * @param name The name of the file on GoogleDrive.
     * @param id The unique ID of the file on Google Drive.
     */
    public GoogleFile(GoogleFile parent, String name, String id) {
        super(parent, name);
        this.id = id;
    }

    /**
     * Constructs a GoogleFile with a name and an ID. The parent will be null and the file is considered to be at the
     * root of Google Drive.
     * @param name The name of the file
     * @param id The ID of the file on Google Drive.
     */
    public GoogleFile(String name, String id) {
        this(null, name, id);
    }

    /**
     * Constructs the root of Google Drive identified by its ID. Be warned that the ID should be retrieved after the
     * creation of the root.
     */
    public GoogleFile() {
        super("root");
        this.directory = true;
    }

    public String getId() {
        if(isRoot() && this.id == null) return "root";
        else return id;
    }

    public void setId(String id) { this.id = id; }


    /**
     * Indicates if this GoogleFile is the root on Google Drive. The file is considered as root if its name equals
     * {@code root}.
     *
     * @return {@code true} if this GoogleFile is the root on Drive, {@code false} otherwise.
     */
    @Override
    public boolean isRoot() { return "root".equals(this.getName()); }
}
