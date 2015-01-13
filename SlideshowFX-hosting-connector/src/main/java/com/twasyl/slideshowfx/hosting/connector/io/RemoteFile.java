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

package com.twasyl.slideshowfx.hosting.connector.io;

import java.io.Serializable;

/**
 * Represents a file that is hosted remotely on the service.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class RemoteFile implements Serializable {

    protected boolean empty = true;
    protected RemoteFile parent = null;
    protected String name = null;
    protected boolean folder = false;
    protected boolean file = false;

    public RemoteFile(String name) {
        this.name = name;
    }

    public RemoteFile(RemoteFile parent, String name) {
        this(name);
        this.parent = parent;
        this.parent.empty = false;
    }

    /**
     * Indicates if the file is empty.
     * @return {@code true} if the file is empty, {@code false} otherwise.
     */
    public boolean isEmpty() { return this.empty; }

    /**
     * Get the parent of the file.
     * @return The parent of the file.
     */
    public RemoteFile getParent() { return this.parent; }

    /**
     * Set the parent of this file.
     * @param parent The parent of this file.
     */
    public void setParent(RemoteFile parent) { this.parent = parent; }

    /**
     * Get the name of this file.
     * @return The name of this file.
     */
    public String getName() { return this.name; }

    /**
     * Set the name of this file.
     * @param name The name of this file.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Indicates if this folder is the root on the service. Default implementation returns {@code true} if the name is
     * {@code null}.
     * @return {@code true} if the folder is the root on the service, {@code false} otherwise.
     */
    public boolean isRoot() { return this.name == null; }

    /**
     * Indicates if this file is considered as a regular file on the hosting service. If {@link #setFile(boolean)} hasn't
     * been called, {@code false} is the default value.
     * @return {@code true} if the file is considered as a regular file on the hosting service, {@code false} otherwise.
     */
    public boolean isFile() { return file; }

    /**
     * Defines if this file is considered as a regular file on the hosting service.
     * @param file {@code true} to indicates this file is a regular file, {@code false} otherwise.
     * @return This instance of the remote file.
     */
    public RemoteFile setFile(boolean file) {
        this.file = file;
        return this;
    }

    /**
     * Indicates if this file is considered as a folder on the hosting service. If {@link #setFolder(boolean)} hasn't
     * been called, {@code false} is the default value.
     * @return {@code true} if the file is considered as a folder on the hosting service, {@code false} otherwise.
     */
    public boolean isFolder() { return folder; }

    /**
     * Defines if this file is considered as a folder on the hosting service.
     * @param folder {@code true} to indicates this file is a folder, {@code false} otherwise.
     * @return This instance of the remote file.
     */
    public RemoteFile setFolder(boolean folder) {
        this.folder = folder;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("");

        if(this.parent != null) builder.append(this.parent.toString());

        builder.append("/").append(this.isRoot() ? "" : this.name);

        return builder.toString();
    }
}
