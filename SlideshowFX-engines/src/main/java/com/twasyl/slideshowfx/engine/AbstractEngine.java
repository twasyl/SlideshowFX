/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.engine;

import com.twasyl.slideshowfx.utils.PlatformHelper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

/**
 * This class implements {@link IEngine} in order to define base treatments used by all engine defined in SlideshowFX.
 *
 * @author Thierry Wasylczenko
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
     * For example, the archiveExtension for presentations' archives is <code>sfx</code> and for template's <code>sfxt</code>.
     *
     * @param archiveExtension The archiveExtension for each archive files of this engine.
     * @param configurationFilename The name of the configuration file, depending the implementation.
     */
    protected AbstractEngine(String archiveExtension, String configurationFilename) {
        this.archiveExtension = archiveExtension;
        this.configurationFilename = configurationFilename;
    }

    @Override public String getConfigurationFilename() { return this.configurationFilename; }

    @Override public String getArchiveExtension() { return this.archiveExtension; }

    @Override public File getArchive() { return this.archiveFile; }
    @Override public void setArchive(File file) {
        final File oldFile = this.archiveFile;
        this.archiveFile = file;
        PlatformHelper.run(() -> this.propertyChangeSupport.firePropertyChange("archiveFile", oldFile, this.archiveFile));
    }

    @Override
    public File generateWorkingDirectory() {
        return new File(System.getProperty("java.io.tmpdir"), "sfx-" + System.currentTimeMillis());
    }

    @Override public File getWorkingDirectory() { return this.workingDirectory; }
    @Override public void setWorkingDirectory(File workingDirectory) { this.workingDirectory = workingDirectory; }

    @Override
    public String relativizeFromWorkingDirectory(File file) throws NullPointerException {
        if(file == null) throw new NullPointerException("The given file can not be null");
        if(getWorkingDirectory() == null) throw new NullPointerException("The working directory is null");

        return getWorkingDirectory().toPath()
                .relativize(file.toPath())
                .toString().replace(File.separator, "/");
    }

    @Override
    public T readConfiguration() throws NullPointerException, IOException, IllegalAccessException {
        if(getWorkingDirectory() == null) throw new NullPointerException("The working directory is null");
        if(this.configurationFilename == null) throw new NullPointerException("The configuration filename can not be null");
        if(this.configurationFilename.isEmpty()) throw new IllegalArgumentException("The configuration filename can not be empty");

        final File configurationFile = new File(getWorkingDirectory(), this.configurationFilename);

        return this.readConfiguration(configurationFile);
    }

    @Override
    public void writeConfiguration() throws NullPointerException, IOException {
        if(getWorkingDirectory() == null) throw new NullPointerException("The working directory is null");
        if(this.configurationFilename == null) throw new NullPointerException("The configuration filename can not be null");
        if(this.configurationFilename.isEmpty()) throw new IllegalArgumentException("The configuration filename can not be empty");

        final File configurationFile = new File(getWorkingDirectory(), this.configurationFilename);

        this.writeConfiguration(configurationFile);
    }

    @Override public T getConfiguration() { return this.configuration; }
    @Override public void setConfiguration(T configuration) { this.configuration = configuration; }

    @Override public void loadArchive() throws IllegalArgumentException, NullPointerException, IOException, IllegalAccessException {
        this.loadArchive(getArchive());
    }

    @Override public synchronized void saveArchive() throws IllegalArgumentException, IOException {
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
