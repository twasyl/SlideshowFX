package com.twasyl.slideshowfx.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing a JAR file and allowing to manipulate it's attributes easily.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.4
 */
public class Jar implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(Jar.class.getName());

    protected final JarFile jar;
    protected final File file;
    protected Manifest manifest = null;
    protected Attributes manifestAttributes = null;

    /**
     * Creates a {@link Jar} object from the given JAR file.
     *
     * @param file The JAR file.
     * @throws IOException If an error occurs.
     */
    public Jar(final File file) throws IOException {
        this.file = file;
        this.jar = new JarFile(this.file);
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
        if (this.jar != null) {
            this.jar.close();
        }
    }

    /**
     * Get the {@link File} of this JAR.
     *
     * @return The {@link File} of this JAR.
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the {@link InputStream} associated to the given {@link JarEntry entry}.
     *
     * @param entry The entry to get the input stream for.
     * @return The {@link InputStream} for the given entry.
     * @throws IOException
     */
    public InputStream getInputStream(final JarEntry entry) throws IOException {
        return this.jar.getInputStream(entry);
    }

    /**
     * Get the {@link Manifest} of this JAR.
     *
     * @return The {@link Manifest} of this JAR.
     */
    public final Manifest getManifest() {
        if (this.manifest == null) {
            try {
                this.manifest = this.jar.getManifest();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not retrieve the MANIFEST file of the JAR", e);
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
            final Manifest manifest = getManifest();

            if (manifest != null) {
                this.manifestAttributes = manifest.getMainAttributes();
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
        final JarEntry entry = this.jar.getJarEntry("META-INF/icon.png");
        return entry;
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
