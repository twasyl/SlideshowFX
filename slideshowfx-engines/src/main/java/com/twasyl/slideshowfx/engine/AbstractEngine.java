package com.twasyl.slideshowfx.engine;

import com.twasyl.slideshowfx.utils.PlatformHelper;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

/**
 * This class implements {@link IEngine} in order to define base treatments used by all engine defined in SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public abstract class AbstractEngine<T extends IConfiguration> implements IEngine<T> {

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected final String configurationFilename;
    protected final String archiveExtension;
    protected File archiveFile;
    protected File workingDirectory;
    protected T configuration;

    /**
     * Creates an instance of the engine and set the archiveExtension all archive files of this engine must have.
     * For example, the archiveExtension for presentations' archives is {@code sfx} and for template's <code>sfxt</code>.
     *
     * @param archiveExtension      The archiveExtension for each archive files of this engine.
     * @param configurationFilename The name of the configuration file, depending the implementation.
     */
    protected AbstractEngine(String archiveExtension, String configurationFilename) {
        this.archiveExtension = archiveExtension;
        this.configurationFilename = configurationFilename;
    }

    /**
     * Checks that the working directory is valid. Currently this checks if the {@link #getWorkingDirectory()} returns
     * {@code null} and throws a {@link NullPointerException} in that case.
     */
    private void validateWorkingDirectory() {
        if (getWorkingDirectory() == null) throw new NullPointerException("The working directory is null");
    }

    @Override
    public String getConfigurationFilename() {
        return this.configurationFilename;
    }

    @Override
    public String getArchiveExtension() {
        return this.archiveExtension;
    }

    @Override
    public File getArchive() {
        return this.archiveFile;
    }

    @Override
    public void setArchive(File file) {
        final File oldFile = this.archiveFile;
        this.archiveFile = file;
        this.propertyChangeSupport.firePropertyChange("archiveFile", oldFile, this.archiveFile);
    }

    @Override
    public File generateWorkingDirectory() {
        return new File(System.getProperty("java.io.tmpdir"), "sfx-" + System.currentTimeMillis());
    }

    @Override
    public File getWorkingDirectory() {
        return this.workingDirectory;
    }

    @Override
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public String relativizeFromWorkingDirectory(File file) {
        if (file == null) throw new NullPointerException("The given file can not be null");
        validateWorkingDirectory();

        return getWorkingDirectory().toPath()
                .relativize(file.toPath())
                .toString().replace(File.separator, "/");
    }

    @Override
    public T readConfiguration() throws IOException, IllegalAccessException {
        validateWorkingDirectory();
        if (this.configurationFilename == null)
            throw new NullPointerException("The configuration filename can not be null");
        if (this.configurationFilename.isEmpty())
            throw new IllegalArgumentException("The configuration filename can not be empty");

        final File configurationFile = new File(getWorkingDirectory(), this.configurationFilename);

        return this.readConfiguration(configurationFile);
    }

    @Override
    public T readConfiguration(File configurationFile) throws IOException, IllegalAccessException {
        validateWorkingDirectory();
        if (configurationFile == null) throw new NullPointerException("The configuration file can not be null");
        if (!configurationFile.exists()) throw new FileNotFoundException("The configuration file does not exist");
        if (!configurationFile.canRead()) throw new IllegalAccessException("The configuration file can not be read");

        final Reader reader = new DefaultCharsetReader(configurationFile);

        return this.readConfiguration(reader);
    }

    @Override
    public void writeConfiguration() throws IOException {
        validateWorkingDirectory();
        if (this.configurationFilename == null)
            throw new NullPointerException("The configuration filename can not be null");
        if (this.configurationFilename.isEmpty())
            throw new IllegalArgumentException("The configuration filename can not be empty");

        final File configurationFile = new File(getWorkingDirectory(), this.configurationFilename);

        this.writeConfiguration(configurationFile);
    }

    @Override
    public void writeConfiguration(File configurationFile) throws IOException {
        validateWorkingDirectory();
        if (configurationFile == null) throw new NullPointerException("The configuration file can not be null");

        final DefaultCharsetWriter writer = new DefaultCharsetWriter(configurationFile);

        this.writeConfiguration(writer);
    }

    @Override
    public T getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(T configuration) {
        this.configuration = configuration;
    }

    @Override
    public void loadArchive() throws IOException, IllegalAccessException {
        this.loadArchive(getArchive());
    }

    @Override
    public synchronized void saveArchive() throws IOException {
        this.saveArchive(getArchive());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }
}
