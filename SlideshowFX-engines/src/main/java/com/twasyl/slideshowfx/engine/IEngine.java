package com.twasyl.slideshowfx.engine;

import java.io.*;

/**
 * Tis interface represents the base for engine used in SlideshowFX.
 * It will be useful for the Presentation engine as well as the template engine.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public interface IEngine<T extends IConfiguration> {

    /**
     * This method checks if the configuration of the engine is valid or not.
     * @return true if the configuration is valid, otherwise an exception should be thrown.
     * @throws EngineException If an error is encountered, an exception will be raised. The exception
     * should be a {@link EngineException} or a subclass of it.
     */
    boolean checkConfiguration() throws EngineException;

    /**
     * Get the name of the configuration file of this engine.
     * @return The name of the configuration file of this engine.
     */
    String getConfigurationFilename();

    /**
     * Get the configuration of this engine.
     *
     * @return The configuration of this engine that should be read from the configuration file.
     */
    T getConfiguration();

    /**
     * Set the configuration for this engine. The configuration should have been read from the configuration file.
     * @param configuration The new configuration for this engine.
     */
    void setConfiguration(T configuration);

    /**
     * Read the configuration file that is stored in the working directory and named according the engine's implementation.
     * This method calls {@link #readConfiguration(File)}.
     *
     * @return The configuration read from the default configuration file.
     * @throws NullPointerException If the given file is null.
     * @throws IOException If the configuration is not found.
     * @throws IllegalAccessException If the configuration file can not be read.
     */
    T readConfiguration() throws NullPointerException, IOException, IllegalAccessException;

    /**
     * Reads the configuration of the engine and store it in a {@link IConfiguration} object.
     *
     * @param configurationFile The file that contains the configuration.
     * @return The configuration read from the file.
     * @throws NullPointerException If the working directory or the configuration filename is null.
     * @throws IllegalArgumentException If the configuration filename is empty.
     * @throws FileNotFoundException If the configuration is not found.
     * @throws IllegalAccessException If the configuration file can not be read.
     */
    T readConfiguration(File configurationFile) throws NullPointerException, IllegalArgumentException, IOException, IllegalAccessException;

    /**
     * Reads the configuration of the engine and store it in a {@link IConfiguration} object.
     *
     * @param reader The file that contains the configuration.
     * @return The configuration read from the reader.
     * @throws NullPointerException If the reader is {@code null}.
     * @throws IOException If an error occurs when reading the reader.
     */
    T readConfiguration(Reader reader) throws NullPointerException, IOException;

    /**
     * Write the configuration in the file that is stored in the working directory and named according the engine's implementation.
     * This method calls {@link #writeConfiguration(File)}.
     *
     * @throws NullPointerException If the given file is null.
     * @throws IOException If the configuration is not found.
     */
    void writeConfiguration() throws NullPointerException, IOException;

    /**
     * Writes this engine's configuration into the given file.
     *
     * @param configurationFile The file where the configuration will be written.
     * @throws NullPointerException If the configurationFile is null.
     * @throws IOException If an error occurs while trying to write the configuration.
     */
    void writeConfiguration(File configurationFile) throws NullPointerException, IOException;

    /**
     * Writes this engine's configuration into the given instance of {@link Writer}. The {@link Writer}
     * is not closed at the end of the process.
     *
     * @param writer The writer where the configuration will be written.
     * @throws NullPointerException If the configurationFile is null.
     * @throws IOException If an error occurs while trying to write the configuration.
     */
    void writeConfiguration(final Writer writer) throws NullPointerException, IOException;

    /**
     * Generates a working directory for the given engine. The directory is located in the temporary folder of the system
     * determined by <code>System.getProperty("java.io.tmpdir")</code> followed by a timestamp to ensure unicity.
     * The name is of the following pattern: <code>sfx-[timestamp]</code>.
     *
     * @return the working directory that has been generated.
     */
    File generateWorkingDirectory();

    /**
     * Get the working directory of this engine archive. Generally the working folder should be a generated temporary folder.
     * @return The file corresponding to the working directory of this engine.
     */
    File getWorkingDirectory();

    /**
     * Set the working directory of this engine.
     *
     * @param workingDirectory The new working directory of this engine.
     */
    void setWorkingDirectory(File workingDirectory);

    /**
     * Get the relative path from the current working directory for the given file.
     *
     * @param file The file to determine the relative path for.
     * @return The String representing the relative path from the working directory.
     * @throws NullPointerException If the given file or the working directory is null.
     */
    String relativizeFromWorkingDirectory(File file) throws NullPointerException;

    /**
     * Get the archiveExtension an archive of this engine must have. For example this method will return
     * <code>sfx</code> for a presentation archive and <code>sfxt</code> for a template archive.
     *
     * @return the archiveExtension which is valid for this engine.
     */
    String getArchiveExtension();

    /**
     * Get the archive file of this engine.
     *
     * @return the file corresponding to the archive of this engine. If the archive has not been set or saved,
     * return <code>null</code>.
     */
    File getArchive();

    /**
     * Set the archive file for this engine. The file must have the correct archiveExtension for this engine.
     * The archiveExtension must be the one returned by {#getArchiveExtension}.
     * @param file The new archive file
     */
    void setArchive(File file);

    /**
     * This methods load an archive for this engine. This method calls {@link #loadArchive(File)}
     * with the current archive file.
     * @throws IllegalArgumentException If the archiveExtension of the archive is not valid.
     * @throws NullPointerException If the given file is null.
     * @throws IOException If the file is not found.
     * @throws IllegalAccessException If the file can not be read.
     */
    void loadArchive() throws IllegalArgumentException, NullPointerException, IOException, IllegalAccessException;

    /**
     * Load the content of the given archive file. The file must have the correct archiveExtension for this engine.
     * The archiveExtension must be the same as the one returned by the {#getArchiveExtension} method.
     *
     * @param file the archive file to load.
     * @throws IllegalArgumentException If the archiveExtension of the archive is not valid.
     * @throws NullPointerException If the given file is null.
     * @throws FileNotFoundException If the file is not found.
     * @throws IllegalAccessException If the file can not be read.
     */
    void loadArchive(File file) throws IllegalArgumentException, NullPointerException, IOException, IllegalAccessException;

    /**
     * Save the content in the archive file. The content is retrieved for the current working directory returned by {#getWorkingDirectory}.
     * This method calls {@link #saveArchive(File)} with the
     * current archive file.
     *
     * @throws IllegalArgumentException If an error occurred when saving the archive.
     * @throws IOException If an error occurred when saving the archive.
     */
    void saveArchive() throws IllegalArgumentException, IOException;

    /**
     * Save the content of the engine into an archive. The content is retrieved for the current working directory returned by {#getWorkingDirectory}.
     * The provided file must have the archiveExtension corresponding to
     * the engine or an exception will be raised. To ensure the archiveExtension is valid, it is tested with the {#getArchiveExtension} method.
     *
     * @param file The file archive where the engine's content will be saved.
     * @throws IllegalArgumentException If the given file has not the correct archiveExtension for this engine.
     * @throws IOException If an error occurred when saving the archive.
     */
    void saveArchive(File file) throws IllegalArgumentException, IOException;
}
