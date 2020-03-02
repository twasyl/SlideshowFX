package com.twasyl.slideshowfx.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * Class representing a JAR file and allowing to manipulate it's attributes easily.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.4
 */
public class Jar implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(Jar.class.getName());

    protected byte[] stream = null;
    protected Manifest manifest = null;
    protected Attributes manifestAttributes = null;

    /**
     * Creates a {@link Jar} object from the given JAR file.
     *
     * @param file The JAR file.
     * @throws IOException If an error occurs.
     */
    public Jar(final File file) throws IOException {
        this(new FileInputStream(file));
    }

    public Jar(final InputStream input) throws IOException {
        if (input != null) {
            final byte[] buffer = new byte[512];
            int bytesRead;

            try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
                this.stream = output.toByteArray();
            }
        }
    }

    /**
     * Get the {@link Jar} for the given {@link Class}. If the class is {@code null}, then {@code null} will be
     * returned.
     *
     * @param clazz The class to get the JAR for.
     * @return The {@link Jar} instance.
     * @throws URISyntaxException If the {@link File} of the JAR can not be determined from the class.
     * @throws IOException        If the {@link Jar} can not be constructed.
     */
    public static Jar fromClass(final Class clazz) throws URISyntaxException, IOException {
        final File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        return new Jar(file);
    }

    @Override
    public void close() throws IOException {
        if (this.stream != null) {
            this.stream = null;
        }
    }

    /**
     * Get the {@link InputStream} associated to the given {@link JarEntry entry}.
     *
     * @param entry The entry to get the input stream for.
     * @return The {@link InputStream} for the given entry.
     * @throws IOException
     */
    public InputStream getInputStream(final JarEntry entry) throws IOException {
        JarEntry iteratedEntry;

        if (entry != null) {
            final String entryName = entry.getName();

            try (final JarInputStream jarStream = buildJarInputStream()) {
                while ((iteratedEntry = jarStream.getNextJarEntry()) != null) {
                    if (entryName.equals(iteratedEntry.getName())) {
                        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                            final byte[] buffer = new byte[1024];
                            int bytesRead;

                            while ((bytesRead = jarStream.read(buffer)) != -1) {
                                output.write(buffer, 0, bytesRead);
                            }

                            output.flush();
                            return new ByteArrayInputStream(output.toByteArray());
                        }
                    }
                }
            }
        }

        return null;
    }

    private JarInputStream buildJarInputStream() throws IOException {
        return new JarInputStream(new ByteArrayInputStream(this.stream));
    }

    /**
     * Get the {@link Manifest} of this JAR.
     *
     * @return The {@link Manifest} of this JAR.
     */
    public final Manifest getManifest() {
        if (this.manifest == null) {
            try (final JarInputStream jarStream = buildJarInputStream()) {
                this.manifest = jarStream.getManifest();
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not retrieve the manifest", e);
            }
        }

        return this.manifest;
    }

    /**
     * Get the attributes contained in the {@code MANIFEST.MF} of the JAR.
     *
     * @return The attributes contained in the {@code MANIFEST.MF} file of the JAR.
     */
    public final Attributes getManifestAttributes() {
        if (this.manifestAttributes == null) {
            final Manifest retrievedManifest = getManifest();

            if (retrievedManifest != null) {
                this.manifestAttributes = retrievedManifest.getMainAttributes();
            }
        }

        return this.manifestAttributes;
    }

    /**
     * Get the value of an attribute stored in the MANIFEST. If the value is {@code null} or
     * empty, the default value will be returned.
     *
     * @param name         The name of the attribute to retrieve the value for.
     * @param defaultValue The default value to return if the original value is {@code null} or empty.
     * @return The value of the attribute.
     */
    public final String getManifestAttributeValue(final String name, final String defaultValue) {
        final Attributes attributes = this.getManifestAttributes();
        final String value = attributes == null ? null : attributes.getValue(name);

        if (value == null || value.isEmpty()) return defaultValue;
        else return value;
    }

    /**
     * Get an entry of this JAR.
     *
     * @param entryName The name of the entry to get.
     * @return The {@link JarEntry} or {@code null} if not found.
     */
    public final JarEntry getEntry(final String entryName) {
        JarEntry entry;

        try (final JarInputStream jarStream = buildJarInputStream()) {
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return entry;
                }
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error getting the entry " + entryName, e);
        }

        return null;
    }

    /**
     * Get the value of the attribute {@link Attributes.Name#IMPLEMENTATION_VERSION}.
     *
     * @return The value of the attribute {@link Attributes.Name#IMPLEMENTATION_VERSION} or {@code null} if not found.
     */
    public final String getImplementationVersion() {
        return getManifestAttributeValue(Attributes.Name.IMPLEMENTATION_VERSION.toString(), null);
    }
}
