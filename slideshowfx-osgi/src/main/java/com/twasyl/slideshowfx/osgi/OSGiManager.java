package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.engine.presentation.Presentations;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.plugin.InstalledPlugin;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.osgi.framework.Constants.*;

/**
 * This class manages all OSGi bundles: from installation to uninstallation. It also starts the OSGi container as well
 * as it can stop it properly.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class OSGiManager {
    private static final Logger LOGGER = Logger.getLogger(OSGiManager.class.getName());
    private static OSGiManager singleton = null;

    public static final String PRESENTATION_FOLDER = "presentation.folder";
    public static final String PRESENTATION_RESOURCES_FOLDER = "presentation.resources.folder";

    protected Framework osgiFramework;
    protected File pluginsDirectory;
    protected File osgiCache;

    /**
     * Default constructor of the class.
     */
    protected OSGiManager() {
        this.pluginsDirectory = GlobalConfiguration.getPluginsDirectory();
        this.osgiCache = new File(GlobalConfiguration.getApplicationDirectory(), "felix-cache");
    }

    public static final synchronized OSGiManager getInstance() {
        if (OSGiManager.singleton == null) {
            OSGiManager.singleton = new OSGiManager();
        }

        return OSGiManager.singleton;
    }

    /**
     * Start the OSGi container.
     */
    public void start() {
        final Map configurationMap = new HashMap<>();
        configurationMap.put(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        configurationMap.put(FRAMEWORK_STORAGE, this.osgiCache.getAbsolutePath().replaceAll("\\\\", "/"));
        configurationMap.put(FRAMEWORK_BUNDLE_PARENT, FRAMEWORK_BUNDLE_PARENT_APP);

        final StringJoiner bootdelegation = new StringJoiner(",");
        bootdelegation.add("com.twasyl.slideshowfx.markup")
                .add("com.twasyl.slideshowfx.content.extension")
                .add("com.twasyl.slideshowfx.hosting.connector")
                .add("com.twasyl.slideshowfx.hosting.connector.io")
                .add("com.twasyl.slideshowfx.hosting.connector.exceptions")
                .add("com.twasyl.slideshowfx.snippet.executor")
                .add("com.twasyl.slideshowfx.osgi")
                .add("com.twasyl.slideshowfx.engine.*")
                .add("com.twasyl.slideshowfx.global.configuration")
                .add("com.twasyl.slideshowfx.utils.*")
                .add("com.twasyl.slideshowfx.utils")
                .add("com.twasyl.slideshowfx.plugin")
                .add("com.twasyl.slideshowfx.server.beans.quiz")
                .add("com.twasyl.slideshowfx.icons")
                .add("com.twasyl.slideshowfx.ui.controls")
                .add("com.twasyl.slideshowfx.ui.controls.validators")
                .add("sun.misc")
                .add("org.w3c.*")
                .add("javax.*")
                .add("javafx.*")
                .add("com.sun.javafx");
        configurationMap.put(FRAMEWORK_BOOTDELEGATION, bootdelegation.toString());

        // Starting OSGi
        final FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        osgiFramework = frameworkFactory.newFramework(configurationMap);
        try {
            osgiFramework.start();
            LOGGER.fine("OSGI container has bee started successfully");
        } catch (BundleException | OverlappingFileLockException e) {
            LOGGER.log(SEVERE, "Can not start OSGi server", e);
            try {
                osgiFramework.stop();
            } catch (BundleException e1) {
                LOGGER.log(SEVERE, "Can not correctly abort OSGi starting process", e1);
            } finally {
                this.osgiFramework = null;
            }
        }
    }

    /**
     * Start the OSGi container and deploy all plugins in the plugins' directory.
     */
    public void startAndDeploy() {
        start();

        if (this.osgiFramework != null) {
            // Deploy initially present plugins
            if (!this.pluginsDirectory.exists() && !this.pluginsDirectory.mkdirs()) {
                LOGGER.log(SEVERE, "Can not create plugins directory");
            }

            if (this.pluginsDirectory.exists()) {
                Arrays.stream(this.pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar")))
                        .forEach(file -> {
                            try {
                                this.deployBundle(file, false);
                            } catch (IOException e) {
                                LOGGER.log(WARNING, "Can not deploy bundle", e);
                            }
                        });

                Arrays.stream(this.osgiFramework.getBundleContext().getBundles())
                        .filter(this::isPluginInactive)
                        .forEach(this::startBundle);
            }
        }
    }

    /**
     * Stop the OSGi container.
     */
    public void stop() {
        if (osgiFramework != null) {
            try {
                osgiFramework.stop();
                osgiFramework.waitForStop(0);
            } catch (BundleException e) {
                LOGGER.log(SEVERE, "Can not stop Felix", e);
            } catch (InterruptedException e) {
                LOGGER.log(SEVERE, "Can not wait for stopping Felix", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Indicates if the instance of the OSGi manager has been started.
     *
     * @return {@code true} if the manager is started, {@code false} otherwise.
     */
    public boolean isStarted() {
        return this.osgiFramework != null && this.osgiFramework.getState() == Framework.ACTIVE;
    }

    /**
     * Deploys a bundleFile in the OSGi container and start it. This method copies the given bundleFile to directory of
     * plugins and then deploys it.
     * If the bundle is already in the plugins' directory, it is simply deployed.
     * If the plugin exists in a more recent version, the given plugin will not be installed.
     * If the plugin is more recent than an already installed version, the old version is uninstalled and the new one
     * is installed.
     *
     * @param bundleFile The bundleFile to deploy.
     * @return The installed service.
     * @throws IllegalArgumentException If the bundleFile is not a directory.
     * @throws FileNotFoundException    If the bundleFile is not found.
     * @throws NullPointerException     If the bundleFile is null.
     */
    public Object deployBundle(File bundleFile) throws IOException {
        return this.deployBundle(bundleFile, true);
    }

    /**
     * Deploys a bundleFile in the OSGi container. This method copies the given bundleFile to directory of plugins and then deploys it.
     * If the bundle is already in the plugins' directory, it is simply deployed.
     * If the plugin exists in a more recent version, the given plugin will not be installed.
     * If the plugin is more recent than an already installed version, the old version is uninstalled and the new one
     * is installed.
     *
     * @param bundleFile The bundleFile to deploy.
     * @param start      Indicate if the bundle should be started.
     * @return The installed service.
     * @throws IllegalArgumentException If the bundleFile is not a directory.
     * @throws FileNotFoundException    If the bundleFile is not found.
     * @throws NullPointerException     If the bundleFile is null.
     */
    protected Object deployBundle(final File bundleFile, final boolean start) throws IOException {
        if (bundleFile == null) throw new NullPointerException("The bundleFile to deploy is null");
        if (!bundleFile.exists()) throw new FileNotFoundException("The bundleFile does not exist");
        if (!bundleFile.isFile()) throw new IllegalArgumentException("The bundleFile has to be a file");

        if (!bundleFile.getParentFile().toPath().normalize().equals(this.pluginsDirectory.toPath())) {
            Files.copy(bundleFile.toPath(), this.pluginsDirectory.toPath().resolve(bundleFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        }

        Bundle bundle = null;
        try {
            bundle = this.osgiFramework.getBundleContext()
                    .installBundle(String.format("file:%1$s/%2$s", this.pluginsDirectory.getAbsolutePath(), bundleFile.getName()));
        } catch (BundleException e) {
            LOGGER.log(WARNING, "Can not install bundle", e);
        }

        if (bundle != null) {
            final boolean isPluginInAnotherVersionInstalled = isPluginInAnotherVersionInstalled(bundle);
            final boolean isPluginMostRecent = isPluginMostRecent(bundle);

            if (isPluginInAnotherVersionInstalled && isPluginMostRecent) {
                final Bundle pluginInAnotherVersion = getPluginInAnotherVersion(bundle);

                uninstallBundle(pluginInAnotherVersion);
                if (start) {
                    startBundle(bundle);
                }
            } else if (!isPluginInAnotherVersionInstalled && !isPluginActive(bundle) && isPluginMostRecent && start) {
                startBundle(bundle);
            } else if (isPluginInAnotherVersionInstalled) {
                uninstallBundle(bundle);
                bundle = null;
            }
        }

        Object service = null;
        if (bundle != null && bundle.getRegisteredServices() != null && bundle.getRegisteredServices().length > 0) {
            ServiceReference serviceReference = bundle.getRegisteredServices()[0];
            service = osgiFramework.getBundleContext().getService(serviceReference);
        }

        return service;
    }

    /**
     * Starts a given bundle. The bundle must already have been installed in the OSGi framework.
     *
     * @param bundle The bundle to start.
     */
    protected void startBundle(Bundle bundle) {
        try {
            bundle.start();
        } catch (BundleException e) {
            LOGGER.log(WARNING, String.format("Can not install bundle [%1$s] in version [%2$s]", bundle.getSymbolicName(), bundle.getVersion()), e);
        }
    }

    /**
     * Uninstall a bundle from the OSGi container.
     *
     * @param bundle The bundle to uninstall.
     */
    protected void uninstallBundle(Bundle bundle) {
        if (bundle != null) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                LOGGER.log(WARNING, String.format("Can not uninstall bundle [%1$s] in version [%2$s]", bundle.getSymbolicName(), bundle.getVersion().toString()));
            }

            try {
                final File bundleFile = new File(new URL(bundle.getLocation()).getFile());
                bundleFile.deleteOnExit();
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, "Can not determine bundle location", e);
            }
        }
    }

    /**
     * Uninstall a bundle from the OSGi container. If the bundle file is found in the OSGi container, then it is
     * uninstalled and the bundle file is marked for being deleted at the application's shutdown.
     *
     * @param bundleFile The bundle to uninstall.
     * @throws FileNotFoundException If the bundle file doesn't exist.
     * @throws BundleException       If an error occurred while trying to remove the bundle.
     */
    public void uninstallBundle(final File bundleFile) throws FileNotFoundException, BundleException {
        if (bundleFile == null) throw new NullPointerException("The bundleFile to deploy is null");
        if (!bundleFile.exists()) throw new FileNotFoundException("The bundleFile does not exist");
        if (!bundleFile.isFile()) throw new IllegalArgumentException("The bundleFile has to be a file");

        final Path bundlePath = bundleFile.toPath().toAbsolutePath();
        final Bundle[] installedBundles = osgiFramework.getBundleContext().getBundles();
        boolean continueSearching = true;
        int index = 0;

        while (continueSearching && index < installedBundles.length) {
            final Bundle installedBundle = installedBundles[index++];
            final File installedBundleFile;

            try {
                installedBundleFile = new File(new URL(installedBundle.getLocation()).getFile());

                continueSearching = !bundlePath.equals(installedBundleFile.toPath().toAbsolutePath());

                if (!continueSearching) {
                    uninstallBundle(installedBundle);
                }
            } catch (MalformedURLException e) {
                LOGGER.log(Level.FINE, "Can not create the URL of the bundle: " + bundleFile.getName(), e);
            }
        }
    }

    /**
     * Return the list of installed services which are from the given {@code serviceType} class.
     *
     * @param <T>         The type of service.
     * @param serviceType The class of service to look for.
     * @return the list of installed services or an empty list if there is no service corresponding to the given class.
     */
    public <T> List<T> getInstalledServices(Class<T> serviceType) {
        final List<T> services = new ArrayList<>();

        try {
            Collection<ServiceReference<T>> references =
                    osgiFramework.getBundleContext().getServiceReferences(serviceType, "(objectClass=" + serviceType.getName() + ")");

            references.stream().forEach(ref -> services.add(osgiFramework.getBundleContext().getService(ref)));
        } catch (InvalidSyntaxException e) {
            LOGGER.log(WARNING, "Can not list all installed service of type " + serviceType.getName());
        }

        return services;
    }

    /**
     * Get the list of {@link InstalledPlugin} of the given type.
     *
     * @param pluginType The type of the plugin to list.
     * @param <T>        The type of the plugins.
     * @return The list containing all installed plugins of the desired type.
     */
    public <T extends IPlugin> List<InstalledPlugin> getInstalledPlugins(Class<T> pluginType) {
        final List<InstalledPlugin> installedPlugins = new ArrayList<>();

        try {
            final Collection<ServiceReference<T>> services =
                    osgiFramework.getBundleContext().getServiceReferences(pluginType, "(objectClass=" + pluginType.getName() + ")");
            installedPlugins.addAll(
                    services.stream()
                            .map(service -> {
                                final Bundle bundle = service.getBundle();
                                return new InstalledPlugin(bundle.getHeaders().get("Bundle-Name"), bundle.getVersion().toString());
                            })
                            .sorted((plugin1, plugin2) -> plugin1.getName().compareTo(plugin2.getName()))
                            .collect(Collectors.toList()));
        } catch (InvalidSyntaxException e) {
            LOGGER.log(WARNING, "Can not list all installed plugin of type " + pluginType.getName());
        }

        return installedPlugins;
    }

    /**
     * Get the list of active plugins.
     *
     * @return The list of active plugins.
     */
    public List<File> getActivePlugins() {
        return Arrays.stream(this.osgiFramework.getBundleContext().getBundles())
                .filter(bundle -> !SYSTEM_BUNDLE_LOCATION.equals(bundle.getLocation()))
                .map(bundle -> {
                    try {
                        return new File(new URL(bundle.getLocation()).getFile());
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "Can not determine plugin location", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
    protected boolean isPluginMostRecent(final Bundle plugin) {
        boolean isMostRecent;

        final Version installedPluginVersion = Arrays.stream(osgiFramework.getBundleContext().getBundles())
                .filter(installedPlugin -> installedPlugin.getSymbolicName().equals(plugin.getSymbolicName()))
                .map(Bundle::getVersion)
                .findFirst()
                .orElse(null);

        if (installedPluginVersion != null) {
            isMostRecent = plugin.getVersion().compareTo(installedPluginVersion) >= 0;
        } else {
            isMostRecent = true;
        }

        return isMostRecent;
    }

    /**
     * Checks if a given plugin is already installed in another version in the OSGi framework. The plugin's match is
     * performed on the {@link Bundle#getSymbolicName() symbolic name} of the plugin.
     *
     * @param plugin The plugin to check.
     * @return {@code true} if the plugin is installed in another version, {@code false} otherwise.
     */
    protected boolean isPluginInAnotherVersionInstalled(final Bundle plugin) {
        return Arrays.stream(osgiFramework.getBundleContext().getBundles())
                .anyMatch(installedPlugin -> {
                    boolean isSameName = installedPlugin.getSymbolicName().equals(plugin.getSymbolicName());
                    boolean isNotSameVersion = !installedPlugin.getVersion().equals(plugin.getVersion());

                    return isSameName && isNotSameVersion;
                });
    }

    /**
     * Get the other version of the given plugin. The plugin's match is performed on the
     * {@link Bundle#getSymbolicName() symbolic name} of the plugin.
     *
     * @param plugin The plugin to check.
     * @return The plugin in the other version of the given plugin, {@code null} if not found.
     */
    protected Bundle getPluginInAnotherVersion(final Bundle plugin) {
        return Arrays.stream(osgiFramework.getBundleContext().getBundles())
                .filter(installedPlugin -> {
                    boolean isSameName = installedPlugin.getSymbolicName().equals(plugin.getSymbolicName());
                    boolean isNotSameVersion = !installedPlugin.getVersion().equals(plugin.getVersion());

                    return isSameName && isNotSameVersion;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the given plugin is active or not. A plugin is considered active if it's state is equal to {@link Bundle#ACTIVE}
     * or {@link Bundle#STARTING}.
     *
     * @param plugin The plugin to check.
     * @return {@code true} if the plugin is active, {@code false} otherwise.
     */
    protected boolean isPluginActive(final Bundle plugin) {
        return Bundle.ACTIVE == plugin.getState() || Bundle.STARTING == plugin.getState();
    }

    /**
     * Check if the given plugin is inactive or not.
     *
     * @param plugin The plugin to check.
     * @return {@code true} if the plugin is inactive, {@code false} otherwise.
     * @see #isPluginActive(Bundle)
     */
    protected boolean isPluginInactive(final Bundle plugin) {
        return !isPluginActive(plugin);
    }
}
