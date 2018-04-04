package com.twasyl.slideshowfx.global.configuration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Base64;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * Represent a presentation opened recently by SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class RecentPresentation extends File implements Comparable<File> {
    private LocalDateTime openedDateTime;
    private String normalizedPath;
    private String id;

    public RecentPresentation(final String path, final LocalDateTime openedDateTime) {
        super(path);
        this.openedDateTime = openedDateTime;
    }

    public RecentPresentation(final String parent, final String child, final LocalDateTime openedDateTime) {
        super(parent, child);
        this.openedDateTime = openedDateTime;
    }

    public RecentPresentation(final File parent, final String child, final LocalDateTime openedDateTime) {
        super(parent, child);
        this.openedDateTime = openedDateTime;
    }

    public LocalDateTime getOpenedDateTime() {
        return openedDateTime;
    }

    public void setOpenedDateTime(LocalDateTime openedDateTime) {
        this.openedDateTime = openedDateTime;
    }

    /**
     * Get the ID of this recent presentation. The ID is the value returned by {@link #getNormalizedPath()} encoded in
     * Base 64.
     *
     * @return The ID of this recent presentation.
     */
    public String getId() {
        if (this.id == null) {
            this.id = Base64.getEncoder().encodeToString(this.getNormalizedPath().getBytes(getDefaultCharset()));
        }

        return this.id;
    }

    /**
     * Get the normalized path of this recent presentation. The normalization consists of replacing all back slashes
     * by forward slashes in the {@link #getAbsolutePath() absolute path} of this recent presentation.
     *
     * @return The normalized path of this recent presentation.
     */
    public String getNormalizedPath() {
        if (normalizedPath == null) {
            this.normalizedPath = getAbsolutePath().replaceAll("\\\\", "/");
        }

        return this.normalizedPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final RecentPresentation that = (RecentPresentation) o;

        return getNormalizedPath() != null ? getNormalizedPath().equals(that.getNormalizedPath()) : that.getNormalizedPath() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getNormalizedPath() != null ? getNormalizedPath().hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(File o) {
        if (o == null) {
            return 1;
        } else if (o instanceof RecentPresentation) {
            final RecentPresentation other = (RecentPresentation) o;
            return this.getNormalizedPath().compareTo(other.getNormalizedPath());
        } else {
            return 0;
        }
    }
}
