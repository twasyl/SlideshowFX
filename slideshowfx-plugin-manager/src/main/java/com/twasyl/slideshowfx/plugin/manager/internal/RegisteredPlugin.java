package com.twasyl.slideshowfx.plugin.manager.internal;

import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.utils.Jar;
import com.twasyl.slideshowfx.utils.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * A registered plugin is a plugin that can be installed, started, stopped and uninstalled. It also hosts a {@link PluginFile}
 * in order to perform installation/uninstallation as well as start/stop operations.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RegisteredPlugin implements Comparable<RegisteredPlugin> {
    private static final Logger LOGGER = Logger.getLogger(RegisteredPlugin.class.getName());

    private final PluginFile file;
    private String name;
    private String version;
    private String description;
    private String iconName;
    private IPlugin instance;
    private PluginClassLoader classLoader;

    public RegisteredPlugin(PluginFile file) {
        this.file = file;
    }

    public PluginFile getFile() {
        return file;
    }

    public String getName() {
        if (this.name == null) {
            final String defaultValue = this.file.getNameOnly();

            try (final Jar jar = this.file.getJar()) {
                if (jar != null) {
                    this.name = jar.getManifestAttributeValue("Plugin-Name", this.file.getNameOnly());
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error retrieving the name", e);
                this.name = defaultValue;
            }
        }

        return name;
    }

    public String getVersion() {
        if (this.version == null) {
            final String defaultValue = "";

            try (final Jar jar = this.file.getJar()) {
                if (jar != null) {
                    this.version = jar.getManifestAttributeValue("Plugin-Version", "");
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error retrieving the version", e);
                this.version = defaultValue;
            }
        }

        return version;
    }

    public String getDescription() {
        if (this.description == null) {
            final String defaultValue = "";

            try (final Jar jar = this.file.getJar()) {
                if (jar != null) {
                    this.description = jar.getManifestAttributeValue("Plugin-Description", defaultValue);
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error retrieving the description", e);
                this.description = defaultValue;
            }
        }

        return description;
    }

    public String getIconName() {
        if (this.iconName == null) {
            final String defaultValue = "";

            try (final Jar jar = this.file.getJar()) {
                if (jar != null) {
                    this.iconName = jar.getManifestAttributeValue("Setup-Wizard-Icon-Name", defaultValue);
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error retrieving the icon name", e);
                this.iconName = defaultValue;
            }
        }

        return iconName;
    }

    /**
     * Get the icon of the plugin. If no icon is present, an empty array is returned.
     *
     * @return The icon of the plugin.
     */
    public byte[] getIcon() {
        final ByteArrayOutputStream icon = new ByteArrayOutputStream();

        try (final Jar jar = this.file.getJar()) {
            if (jar != null) {
                final JarEntry entry = jar.getEntry("META-INF/icon.png");

                if (entry != null) {
                    try (final InputStream iconIn = jar.getInputStream(entry)) {
                        final byte[] buffer = new byte[512];
                        int numberOfBytesRead;

                        while ((numberOfBytesRead = iconIn.read(buffer)) != -1) {
                            icon.write(buffer, 0, numberOfBytesRead);
                        }

                        icon.flush();
                        icon.close();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error retrieving the icon", e);
        }

        return icon.toByteArray();
    }

    public IPlugin getInstance() {
        return instance;
    }

    public boolean isInstanceOf(final Class<? extends IPlugin> clazz) {
        try (final Jar jar = this.file.getJar()) {
            return jar.getEntry("META-INF/services/" + clazz.getName()) != null;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Can not determine instance of " + clazz, e);
            return false;
        }
    }

    public boolean install() {
        if (!isInstalled()) {
            try {
                this.file.unarchive();
                return true;
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not install plugin", e);
                return false;
            }
        }

        return false;
    }

    public void uninstall() {
        this.file.deleteOnExit();
        try {
            IOUtils.deleteDirectory(this.file.getExplodedDir());
        } catch (IOException e) {
            LOGGER.log(WARNING, "Can not uninstall plugin completely", e);
        }
    }

    public boolean isInstalled() {
        return this.file.getExplodedDir().exists();
    }

    public boolean start() throws IOException {
        this.classLoader = PluginClassLoader.newInstance(this.file);

        final ServiceLoader<IPlugin> services = ServiceLoader.load(IPlugin.class, classLoader);
        final Iterator<IPlugin> iterator = services.iterator();

        if (iterator.hasNext()) {
            this.instance = iterator.next();
        }

        return isStarted();
    }

    public void stop() {
        LOGGER.info("Stopping plugin " + this.getName() + " " + this.getVersion());
        this.instance = null;

        if (this.classLoader != null) {
            try {
                this.classLoader.close();
                this.classLoader = null;
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not stop plugin", e);
            }
        }
    }

    public boolean isStarted() {
        return this.instance != null;
    }

    @Override
    public int compareTo(RegisteredPlugin o) {
        return Comparator.comparing(RegisteredPlugin::getName)
                .thenComparing(RegisteredPlugin::getVersion)
                .compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredPlugin that = (RegisteredPlugin) o;
        return file.equals(that.file) &&
                getName().equals(that.getName()) &&
                getVersion().equals(that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, getName(), getVersion());
    }
}
