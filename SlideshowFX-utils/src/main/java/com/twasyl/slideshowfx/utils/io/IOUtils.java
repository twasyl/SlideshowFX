package com.twasyl.slideshowfx.utils.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class provides utility methods on I/O.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class IOUtils {

    /**
     * Deletes the given directory recursively.
     * @param directory The directory to delete.
     * @throws IOException If an error occurs during the deletion.
     * @see #deleteDirectory(Path)
     */
    public static void deleteDirectory(final File directory) throws IOException {
        deleteDirectory(directory.toPath());
    }

    /**
     * Deletes the given directory recursively.
     * @param directory The directory to delete.
     * @throws IOException If an error occurs during the deletion.
     * @see #deleteDirectory(Path)
     */
    public static void deleteDirectory(final Path directory) throws IOException {
        Files.walkFileTree(directory, new DeleteFileVisitor());
    }
}
