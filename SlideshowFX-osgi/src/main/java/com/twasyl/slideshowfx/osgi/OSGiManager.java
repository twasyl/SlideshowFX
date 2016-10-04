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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class manages all OSGi bundles: from installation to uninstallation. It also starts the OSGi container as well
 * as it can stop it properly.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class OSGiManager {
    private static final Logger LOGGER = Logger.getLogger(OSGiManager.class.getName());
    private static Framework osgiFramework;

    /**
     * Start the OSGi container.
     */
    public static void start() {

        Map configurationMap = new HashMap<>();
        // configurationMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, IMarkup.class.getPackage().getName() + "; 1.0.0");
        configurationMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
        configurationMap.put("org.osgi.framework.storage.clean", "onFirstInit");
        configurationMap.put("org.osgi.framework.storage", System.getProperty("user.home") + "/.SlideshowFX/felix-cache");
        configurationMap.put("org.osgi.framework.bundle.parent", "app");

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
                      .add("com.twasyl.slideshowfx.plugin")
                      .add("com.twasyl.slideshowfx.server.beans.quiz")
                      .add("de.jensd.fx.glyphs")
                      .add("de.jensd.fx.glyphs.fontawesome")
                      .add("sun.misc")
                      .add("org.w3c.*")
                      .add("javax.*")
                      .add("javafx.*")
                      .add("com.sun.javafx");
        configurationMap.put("org.osgi.framework.bootdelegation", bootdelegation.toString());
        configurationMap.put("felix.auto.deploy.action", "install,start");

        // Starting OSGi
        FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        osgiFramework = frameworkFactory.newFramework(configurationMap);
        try {
            osgiFramework.start();
            LOGGER.fine("OSGI container has bee started successfully");
        } catch (BundleException e) {
            LOGGER.log(Level.SEVERE, "Can not start OSGi server");
        }

        // Deploying the OSGi DataServices
        // osgiFramework.getBundleContext().registerService(DataServices.class.getName(), new DataServices(), new Hashtable<>());
    }

    /**
     * Start the OSGi container and deploy all plugins in the plugins' directory.
     */
    public static void startAndDeploy() {
        start();

        // Deploy initially present plugins
        if(!GlobalConfiguration.PLUGINS_DIRECTORY.exists()) {
            if(!GlobalConfiguration.PLUGINS_DIRECTORY.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Can not create plugins directory");
            }
        }

        if(GlobalConfiguration.PLUGINS_DIRECTORY.exists()) {
            Arrays.stream(GlobalConfiguration.PLUGINS_DIRECTORY.listFiles((dir, name) -> name.endsWith(".jar")))
                    .forEach(file -> {
                        try {
                            OSGiManager.deployBundle(file);
                        } catch (IOException e) {
                            LOGGER.log(Level.WARNING, "Can not deploy bundle", e);
                        }
                    });
        }
    }

    /**
     * Stop the OSGi container.
     */
    public static void stop() {
        if(osgiFramework != null) {
            try {
                osgiFramework.stop();
                osgiFramework.waitForStop(0);
            } catch (BundleException e) {
                LOGGER.log(Level.SEVERE, "Can not stop Felix", e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Can not wait for stopping Felix", e);
            }
        }
    }

    /**
     * Deploys a bundleFile in the OSGi container. This method copies the given bundleFile to directory of plugins and then deploys it.
     * If the bundle is already in the plugins' directory, it is simply deployed.
     *
     * @param bundleFile The bundleFile to deploy.
     * @throws IllegalArgumentException If the bundleFile is not a directory.
     * @throws FileNotFoundException If the bundleFile is not found.
     * @throws NullPointerException If the bundleFile is null.
     * @return The installed service.
     */
    public static Object deployBundle(File bundleFile) throws IllegalArgumentException, NullPointerException, IOException {
        if(bundleFile == null) throw new NullPointerException("The bundleFile to deploy is null");
        if(!bundleFile.exists()) throw new FileNotFoundException("The bundleFile does not exist");
        if(!bundleFile.isFile()) throw new IllegalArgumentException("The bundleFile has to be a file");

        if(!bundleFile.getParentFile().toPath().normalize().equals(GlobalConfiguration.PLUGINS_DIRECTORY.toPath())) {
            Files.copy(bundleFile.toPath(), GlobalConfiguration.PLUGINS_DIRECTORY.toPath().resolve(bundleFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        }

        Bundle bundle = null;
        try {
            bundle = osgiFramework.getBundleContext()
                    .installBundle(String.format("file:%1$s/%2$s", GlobalConfiguration.PLUGINS_DIRECTORY.getAbsolutePath(), bundleFile.getName()));
        } catch (BundleException e) {
            LOGGER.log(Level.WARNING, "Can not install bundle", e);
        }

        if(bundle != null) {
            try {
                bundle.start();
            } catch (BundleException e) {
                LOGGER.log(Level.WARNING, String.format("Can not install bundle [%1$s]", bundleFile.getName()), e);
            }
        }

        Object service = null;
        if(bundle != null && bundle.getRegisteredServices() != null && bundle.getRegisteredServices().length > 0) {
            ServiceReference serviceReference = bundle.getRegisteredServices()[0];
            service = osgiFramework.getBundleContext().getService(serviceReference);
        }

        return service;
    }

    /**
     * Uninstall a bundle from the OSGi container. If the bundle file is found in the OSGi container, then it is
     * uninstalled and the bundle file is marked for being deleted at the application's shutdown.
     * @param bundleFile The bundle to uninstall.
     * @throws FileNotFoundException If the bundle file doesn't exist.
     * @throws BundleException If an error occurred while trying to remove the bundle.
     */
    public static void uninstallBundle(final File bundleFile) throws FileNotFoundException, BundleException {
        if(bundleFile == null) throw new NullPointerException("The bundleFile to deploy is null");
        if(!bundleFile.exists()) throw new FileNotFoundException("The bundleFile does not exist");
        if(!bundleFile.isFile()) throw new IllegalArgumentException("The bundleFile has to be a file");

        final Bundle[] installedBundles = osgiFramework.getBundleContext().getBundles();
        boolean continueSearching = true;
        int index = 0;

        while(continueSearching && index < installedBundles.length) {
            final Bundle installedBundle = installedBundles[index++];
            final File installedBundleFile;

            try {
                installedBundleFile = new File(new URL(installedBundle.getLocation()).getFile());

                continueSearching = !bundleFile.equals(installedBundleFile);

                if(!continueSearching) {
                    installedBundle.uninstall();
                    installedBundleFile.deleteOnExit();
                }
            } catch (MalformedURLException e) {
                LOGGER.log(Level.FINE, "Can not create the URL of the bundle: " + bundleFile.getName(), e);
            }
        }
    }

    /**
     * Return the list of installed services which are from the given {@code serviceType} class.
     * @param <T> The type of service.
     * @param serviceType The class of service to look for.
     * @return the list of installed services or an empty list if there is no service corresponding to the given class.
     */
    public static <T> List<T> getInstalledServices(Class<T> serviceType) {
        final List<T> services = new ArrayList<>();

        try {
            Collection<ServiceReference<T>> references =
                    osgiFramework.getBundleContext().getServiceReferences(serviceType, "(objectClass=" + serviceType.getName() + ")");

            references.stream().forEach(ref -> services.add(osgiFramework.getBundleContext().getService(ref)));
        } catch (InvalidSyntaxException e) {
            LOGGER.log(Level.WARNING, "Can not list all installed service of type " + serviceType.getName());
        }

        return services;
    }

    /**
     * Get the list of {@link InstalledPlugin} of the given type.
     * @param pluginType The type of the plugin to list.
     * @param <T> The type of the plugins.
     * @return The list containing all installed plugins of the desired type.
     */
    public static <T extends IPlugin> List<InstalledPlugin> getInstalledPlugins(Class<T> pluginType) {
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
            LOGGER.log(Level.WARNING, "Can not list all installed plugin of type " + pluginType.getName());
        }

        return installedPlugins;
    }

    public static final String PRESENTATION_FOLDER = "presentation.folder";
    public static final String PRESENTATION_RESOURCES_FOLDER = "presentation.resources.folder";

    public static Object getPresentationProperty(String property) {
        Object value = null;

        if(Presentations.getCurrentDisplayedPresentation() != null) {
            if(PRESENTATION_FOLDER.equals(property)) {
                value = Presentations.getCurrentDisplayedPresentation().getWorkingDirectory();
            }
            else if(PRESENTATION_RESOURCES_FOLDER.equals(property)) {
                value = Presentations.getCurrentDisplayedPresentation().getTemplateConfiguration().getResourcesDirectory();
            }
        }

        return value;
    }
}
