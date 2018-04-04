package com.twasyl.slideshowfx.hosting.connector.drive.io;

import com.twasyl.slideshowfx.hosting.connector.io.RemoteFile;

/**
 * An specific implementation of a {@link com.twasyl.slideshowfx.hosting.connector.io.RemoteFile} for the representation of a file
 * on Google Drive. A {@link GoogleFile} is composed of a parent (also a
 * GoogleFile), an ID and a name.
 * If a GoogleFile doesn't have a parent, it is considered at the root of Google Drive.
 * The ID of a GoogleFile is the one provided by Google and should not be created other than getting the real one on Drive.
 *
 * @author Thierry Wasylcznko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class GoogleFile extends RemoteFile {

    private String id;
    private boolean directory = false;
    private GoogleFile parent = null;
    private String downloadUrl;

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

    public String getDownloadUrl() { return downloadUrl; }

    /**
     * Set the download URL for this file.
     * @param downloadUrl The new download URL.
     * @return This instance of GoogleFile.
     */
    public GoogleFile setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }
}
