package com.twasyl.slideshowfx.utils.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

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

    /**
     * Dump the given {@link InputStream} into another one. If the given stream is closed or if an error occurs during
     * the dump, {@code null} is returned.
     *
     * @param input The stream to dump.
     * @return a dumped input stream.
     */
    public static InputStream dump(final InputStream input) {
        final byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayInputStream result = null;

        try (final ByteArrayOutputStream dump = new ByteArrayOutputStream()) {
            while ((bytesRead = input.read(buffer)) != -1) {
                dump.write(buffer, 0, bytesRead);
            }

            dump.flush();
            result = new ByteArrayInputStream(dump.toByteArray());
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error dumping InputStream", e);
        }

        return result;
    }
}
