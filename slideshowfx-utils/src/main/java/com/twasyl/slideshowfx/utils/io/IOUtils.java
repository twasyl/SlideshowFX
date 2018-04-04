package com.twasyl.slideshowfx.utils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides utility methods on I/O.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class IOUtils {
    private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());

    /**
     * Deletes the given directory recursively.
     *
     * @param directory The directory to delete.
     * @throws IOException If an error occurs during the deletion.
     * @see #deleteDirectory(Path)
     */
    public static void deleteDirectory(final File directory) throws IOException {
        deleteDirectory(directory.toPath());
    }

    /**
     * Deletes the given directory recursively.
     *
     * @param directory The directory to delete.
     * @throws IOException If an error occurs during the deletion.
     * @see #deleteDirectory(Path)
     */
    public static void deleteDirectory(final Path directory) throws IOException {
        Files.walkFileTree(directory, new DeleteFileVisitor());
    }

    /**
     * Reads a given {@link InputStream} and return it's content to a {@link String}. This method
     * uses the {@link DefaultCharsetReader} in order to read the resource.
     *
     * @param input The input stream to be read.
     * @return The content of the input stream.
     */
    public static String read(final InputStream input) {
        final StringJoiner builder = new StringJoiner("\n");

        try (final BufferedReader reader = new DefaultCharsetReader(input)) {
            reader.lines().forEach(builder::add);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not read the resource content", e);
        }

        return builder.toString();
    }
}
