package com.twasyl.slideshowfx.plugin.manager;

import com.twasyl.slideshowfx.engine.presentation.Presentations;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import com.twasyl.slideshowfx.plugin.manager.internal.RegisteredPlugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * This class manages all SlideshowFX plugins: from installation to uninstallation. It also starts the plugin manager as
 * well as it can stop it properly.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class PluginManager {
    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());
    private static PluginManager singleton = null;

    public static final String PRESENTATION_FOLDER = "presentation.folder";
    public static final String PRESENTATION_RESOURCES_FOLDER = "presentation.resources.folder";

    protected File pluginsDirectory;
    protected Set<RegisteredPlugin> loadedPlugins = new HashSet<>();

    /**
     * Default constructor of the class.
     */
    protected PluginManager() {
        this.pluginsDirectory = GlobalConfiguration.getPluginsDirectory();
    }

    public static synchronized PluginManager getInstance() {
        if (PluginManager.singleton == null) {
            PluginManager.singleton = new PluginManager();
        }

        return PluginManager.singleton;
    }

    /**
     * Start all plugins in the plugins' directory.
     */
    public void start() {
        // Deploy initially present plugins
        if (!this.pluginsDirectory.exists() && !this.pluginsDirectory.mkdirs()) {
            LOGGER.log(SEVERE, "Can not create plugins directory");
        }

        if (this.pluginsDirectory.exists()) {
            this.listMostRecentPluginFiles()
                    .forEach(file -> {
                        try {
                            this.installPlugin(file);
                        } catch (IOException e) {
                            LOGGER.log(WARNING, "Can not deploy bundle", e);
                        }
                    });
        }
    }

    /**
     * List all plugin files and keep only the most recent version for plugin having the same name.
     *
     * @return a collection containing the most recent plugins.
     */
    protected Collection<File> listMostRecentPluginFiles() {
        final Set<RegisteredPlugin> filteredPlugins = new HashSet<>();

        final FileFilter keepPluginFiles = file -> file.exists() && file.getName().endsWith(PluginFile.EXTENSION);

        for (File file : this.pluginsDirectory.listFiles(keepPluginFiles)) {
            try {
                final RegisteredPlugin currentPlugin = new RegisteredPlugin(new PluginFile(file));
                final RegisteredPlugin pluginWithSameName = filteredPlugins.stream()
                        .filter(PluginManager.this.hasSameName(currentPlugin))
                        .findAny()
                        .orElse(null);

                if (pluginWithSameName == null) {
                    filteredPlugins.add(currentPlugin);
                } else if (currentPlugin.getVersion().compareTo(pluginWithSameName.getVersion()) > 0) {
                    filteredPlugins.remove(pluginWithSameName);
                    filteredPlugins.add(currentPlugin);
                }
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error listing most recent plugins", e);
            }
        }

        return filteredPlugins.stream().map(plugin -> plugin.getFile().getAbsoluteFile()).collect(toSet());
    }

    /**
     * Stop all plugins.
     */
    public void stop() {
        this.loadedPlugins.forEach(RegisteredPlugin::stop);
        this.loadedPlugins.clear();
    }

    /**
     * Install a file as plugin and start it. This method copies the given file to directory of
     * plugins and then installs it.
     * If the plugin is already in the plugins' directory, it is simply started.
     * If the plugin exists in a more recent version, the given plugin will not be installed.
     * If the plugin is more recent than an already installed version, the old version is uninstalled and the new one
     * is installed.
     *
     * @param file The file to deploy.
     * @return The installed service.
     * @throws IllegalArgumentException If the file is not a directory.
     * @throws FileNotFoundException    If the file is not found.
     * @throws NullPointerException     If the file is null.
     */
    public IPlugin installPlugin(File file) throws IOException {
        if (file == null) throw new NullPointerException("The file to deploy is null");
        if (!file.exists()) throw new FileNotFoundException("The file does not exist");
        if (!file.isFile()) throw new IllegalArgumentException("The file has to be a file");

        final PluginFile pluginFile;

        if (!file.getParentFile().toPath().normalize().equals(this.pluginsDirectory.toPath())) {
            final Path pluginInsidePluginsDir = this.pluginsDirectory.toPath().resolve(file.getName());
            Files.copy(file.toPath(), pluginInsidePluginsDir, StandardCopyOption.REPLACE_EXISTING);
            pluginFile = new PluginFile(pluginInsidePluginsDir.toAbsolutePath().toFile());
        } else {
            pluginFile = new PluginFile(file.getAbsoluteFile());
        }

        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(pluginFile);

        registeredPlugin.install();

        if (registeredPlugin.isInstalled()) {

            final boolean isPluginInAnotherVersionInstalled = isPluginInAnotherVersionInstalled(registeredPlugin);
            final boolean isPluginMostRecent = isPluginMostRecent(registeredPlugin);

            if (isPluginInAnotherVersionInstalled && isPluginMostRecent) {
                final RegisteredPlugin pluginInAnotherVersion = getPluginInAnotherVersion(registeredPlugin);
                uninstallPlugin(pluginInAnotherVersion);
                startPlugin(registeredPlugin);
            } else if (!isPluginInAnotherVersionInstalled && !registeredPlugin.isStarted() && isPluginMostRecent) {
                startPlugin(registeredPlugin);
            } else if (isPluginInAnotherVersionInstalled) {
                uninstallPlugin(registeredPlugin);
            }
        }

        return registeredPlugin.getInstance();
    }

    /**
     * Starts a given plugin. The plugin must already have been installed before calling this method.
     *
     * @param plugin The plugin to start.
     */
    protected void startPlugin(RegisteredPlugin plugin) {
        try {
            if (plugin.start()) {
                this.loadedPlugins.add(plugin);
            } else {
                LOGGER.warning("The plugin has not been started: " + plugin.getFile().getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(WARNING, String.format("Can not start the plugin [%1$s] in version [%2$s]", plugin.getName(), plugin.getVersion()), e);
        }
    }

    /**
     * Uninstall a plugin.
     *
     * @param plugin The plugin to uninstall.
     */
    protected void uninstallPlugin(RegisteredPlugin plugin) {
        this.loadedPlugins.remove(plugin);
        plugin.stop();
        plugin.uninstall();
    }

    /**
     * Uninstall a plugin. If the plugin file is found, then it is
     * uninstalled and the plugin file is marked for being deleted at the application's shutdown.
     *
     * @param plugin The plugin to uninstall.
     */
    public void uninstallPlugin(final PluginFile plugin) {
        this.loadedPlugins.stream()
                .filter(rp -> Objects.equals(rp.getFile(), plugin))
                .collect(toList())
                .stream()
                .forEach(this::uninstallPlugin);
    }

    /**
     * Return the list of installed services which are from the given {@code serviceType} class.
     *
     * @param <T>         The type of service.
     * @param serviceType The class of service to look for.
     * @return the list of installed services or an empty list if there is no service corresponding to the given class.
     */
    public <T> List<T> getServices(Class<T> serviceType) {
        return this.loadedPlugins.stream()
                .filter(RegisteredPlugin::isStarted)
                .map(RegisteredPlugin::getInstance)
                .filter(serviceType::isInstance)
                .map(serviceType::cast)
                .collect(toList());
    }

    /**
     * Get the list of plugins of the given type.
     *
     * @param pluginType The type of the plugin to list.
     * @param <T>        The type of the plugins.
     * @return The list containing all installed plugins of the desired type.
     */
    public <T extends IPlugin> List<RegisteredPlugin> getPlugins(Class<T> pluginType) {
        return this.loadedPlugins.stream()
                .filter(plugin -> plugin.isInstanceOf(pluginType))
                .collect(toList());
    }

    /**
     * Get the list of active plugins.
     *
     * @return The list of active plugins.
     */
    public List<RegisteredPlugin> getActivePlugins() {
        return this.loadedPlugins.stream()
                .filter(RegisteredPlugin::isStarted)
                .collect(toList());
    }

    public Object getPresentationProperty(String property) {
        Object value = null;

        if (Presentations.getCurrentDisplayedPresentation() != null) {
            if (PRESENTATION_FOLDER.equals(property)) {
                value = Presentations.getCurrentDisplayedPresentation().getWorkingDirectory();
            } else if (PRESENTATION_RESOURCES_FOLDER.equals(property)) {
                value = Presentations.getCurrentDisplayedPresentation().getTemplateConfiguration().getResourcesDirectory();
            }
        }

        return value;
    }

    /**
     * Checks if a given plugin is the most recent compared to installed plugin. If the plugin version is strictly
     * greater than the first plugin's version found then it is considered as the most recent plugin.
     *
     * @param plugin The plugin to check.
     * @return {@code true} if the plugin is the most recent, {@code false} otherwise.
     */
    protected boolean isPluginMostRecent(final RegisteredPlugin plugin) {
        return plugin.getVersion().compareTo(this.loadedPlugins.stream()
                .filter(hasSameName(plugin))
                .max(Comparator.comparing(RegisteredPlugin::getVersion).reversed())
                .orElse(plugin)
                .getVersion()) >= 0;
    }

    /**
     * Checks if a given plugin is already installed in another version. The plugin's match is
     * performed on the {@link RegisteredPlugin#getName() name} of the plugin.
     *
     * @param plugin The plugin to check.
     * @return {@code true} if the plugin is installed in another version, {@code false} otherwise.
     */
    protected boolean isPluginInAnotherVersionInstalled(final RegisteredPlugin plugin) {
        return this.loadedPlugins.stream()
                .filter(hasSameName(plugin).and(isNotSameVersion(plugin)))
                .count() > 0;
    }

    /**
     * Get the other version of the given plugin. The plugin's match is performed on the
     * {@link RegisteredPlugin#getName() name} of the plugin.
     *
     * @param plugin The plugin to check.
     * @return The plugin in the other version of the given plugin, {@code null} if not found.
     */
    protected RegisteredPlugin getPluginInAnotherVersion(final RegisteredPlugin plugin) {
        return this.loadedPlugins.stream()
                .filter(hasSameName(plugin).and(isNotSameVersion(plugin)))
                .findAny()
                .orElse(null);
    }

    private Predicate<RegisteredPlugin> hasSameName(RegisteredPlugin plugin) {
        return p1 -> Objects.equals(p1.getName(), plugin.getName());
    }

    private Predicate<RegisteredPlugin> isNotSameVersion(RegisteredPlugin plugin) {
        return p1 -> !Objects.equals(p1.getVersion(), plugin.getVersion());
    }
}
