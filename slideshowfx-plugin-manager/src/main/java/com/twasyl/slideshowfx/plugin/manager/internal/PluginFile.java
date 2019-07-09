package com.twasyl.slideshowfx.plugin.manager.internal;

import com.twasyl.slideshowfx.utils.Jar;
import com.twasyl.slideshowfx.utils.ZipUtils;
import com.twasyl.slideshowfx.utils.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.logging.Level.WARNING;

/**
 * Represents a plugin file that can be installed to extend the SlideshowFX application. A plugin file is an archive,
 * having the {@code .sfx-plugin} extension and contains everything the plug-in needs to work.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PluginFile extends File {
    private static final Logger LOGGER = Logger.getLogger(PluginFile.class.getName());
    public static final String EXTENSION = ".sfx-plugin";

    public PluginFile(File file) throws IOException {
        this(file.getAbsolutePath());
    }

    public PluginFile(String pathname) throws IOException {
        super(pathname);

        if (!getName().endsWith(EXTENSION)) {
            throw new IOException("The file extension is not correct");
        }
    }

    public String getNameOnly() {
        return getName().substring(0, getName().indexOf(EXTENSION));
    }

    public void unarchive() throws IOException {
        ZipUtils.unzip(this, getExplodedDir());
    }

    public Jar getJar() throws IOException {
        final File jarFile = new File(getExplodedDir(), getNameOnly() + ".jar");
        InputStream input = null;

        if (jarFile.exists()) {
            try (final FileInputStream jarInput = new FileInputStream(jarFile)) {
                input = IOUtils.dump(jarInput);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error getting JarInputStream on extracted plugin", e);
            }
        } else {
            try (final ZipFile zipFile = new ZipFile(this)) {
                final ZipEntry entry = zipFile.getEntry(getNameOnly() + ".jar");

                if (entry != null) {
                    input = IOUtils.dump(zipFile.getInputStream(entry));
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error getting JarInputStream on archive plugin", e);
            }
        }

        return new Jar(input);
    }

    public File getExplodedDir() {
        return new File(this.getParent(), getNameOnly());
    }
}
