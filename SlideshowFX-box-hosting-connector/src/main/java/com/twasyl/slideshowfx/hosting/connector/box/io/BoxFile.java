package com.twasyl.slideshowfx.hosting.connector.box.io;

import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;

/**
 * An specific implementation of a {@link RemoteFile} for the representation of a file
 * on Box. A {@link BoxFile} is composed of a parent (also a BoxFile) and an ID.
 * If a {@link BoxFile} doesn't have a parent, it is considered at the root of Box.
 * The ID of a {@link BoxFile} is the one provided by Box and should not be created other than getting the real one on Box.
 *
 * @author Thierry Wasylcznko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class BoxFile extends RemoteFile {

    private String id;
    private String downloadUrl;

    /**
     * Constructs a {@link BoxFile} according the given parameters. The {@code name} corresponds to the name of the file on
     * Box and the {@code id} to the ID that identifies the file on Box.
     * @param parent The parent of this {@link BoxFile}.
     * @param name The name of the file on Box
     * @param id The unique ID of the file on Box.
     */
    public BoxFile(BoxFile parent, String name, String id) {
        super(parent, name);
        this.id = id;
    }

    /**
     * Constructs a {@link BoxFile} with a name and an ID. The parent will be null and the file is considered to be at the
     * root of Box.
     * @param name The name of the file
     * @param id The ID of the file on Box.
     */
    public BoxFile(String name, String id) {
        this(null, name, id);
    }

    /**
     * Constructs the root of Box identified by its ID. Be warned that the ID should be retrieved after the
     * creation of the root.
     */
    public BoxFile() {
        super(null);
        this.folder = true;
        this.file = false;
    }

    public String getId() {
        if(isRoot() && this.id == null) return "root";
        else return id;
    }

    public void setId(String id) { this.id = id; }

    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Set the download URL for this file.
     * @param downloadUrl The new download URL.
     * @return This instance of {@link BoxFile}.
     */
    public BoxFile setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }
}
