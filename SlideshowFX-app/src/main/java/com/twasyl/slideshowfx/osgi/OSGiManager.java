/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.markup.IMarkup;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class OSGiManager {
    private static final Logger LOGGER = Logger.getLogger(OSGiManager.class.getName());

    private static final File pluginsDirectory = new File(System.getProperty("user.home") + "/.SlideshowFX/plugins");
    private static Framework osgiFramework;
    private static ServiceTracker markupServiceTracker;
    private static ServiceTracker contentExtensionServiceTracker;
    private static ServiceTracker hostingConnectorServiceTracker;

    /**
     * Start the OSGi container.
     */
    public static void start() {

        Map configurationMap = new HashMap<>();
        configurationMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, IMarkup.class.getPackage().getName() + "; 1.0.0");
        configurationMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
        configurationMap.put("org.osgi.framework.storage.clean", "onFirstInit");
        configurationMap.put("org.osgi.framework.storage", System.getProperty("user.home") + "/.SlideshowFX/felix-cache");
        configurationMap.put("org.osgi.framework.bundle.parent", "app");
        configurationMap.put("org.osgi.framework.bootdelegation", "com.twasyl.slideshowfx.markup," +
                "com.twasyl.slideshowfx.content.extension," +
                "com.twasyl.slideshowfx.hosting.connector," +
                "com.twasyl.slideshowfx.hosting.connector.io," +
                "com.twasyl.slideshowfx.osgi," +
                "com.twasyl.slideshowfx.engine.*," +
                "sun.misc," +
                "org.w3c.*," +
                "javax.*," +
                "javafx.*");
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

        try {
            markupServiceTracker = new ServiceTracker(
                    osgiFramework.getBundleContext(),
                    osgiFramework.getBundleContext().createFilter("(objectClass=" + IMarkup.class.getName() + ")"),
                    null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        markupServiceTracker.open();

        try {
            contentExtensionServiceTracker = new ServiceTracker(
                    osgiFramework.getBundleContext(),
                    osgiFramework.getBundleContext().createFilter("(objectClass=" + IContentExtension.class.getName() + ")"),
                    null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        contentExtensionServiceTracker.open();

        try {
            hostingConnectorServiceTracker = new ServiceTracker(
                    osgiFramework.getBundleContext(),
                    osgiFramework.getBundleContext().createFilter("(objectClass=" + IHostingConnector.class.getName() + ")"),
                    null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        hostingConnectorServiceTracker.open();

        // Deploying the OSGi DataServices
        osgiFramework.getBundleContext().registerService(DataServices.class.getName(), new DataServices(), new Hashtable<>());
    }

    /**
     * Start the OSGi container and deploy all plugins in the plugins' directory.
     */
    public static void startAndDeploy() {
        start();

        // Deploy initially present plugins
        if(!pluginsDirectory.exists()) pluginsDirectory.mkdirs();

        Arrays.stream(pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar")))
                .forEach(file -> {
                    try {
                        OSGiManager.deployBundle(file);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Can not deploy bundle", e);
                    }
                });
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
     * @throws java.lang.IllegalArgumentException If the bundleFile is not a directory.
     * @throws java.io.FileNotFoundException If the bundleFile is not found.
     * @throws java.lang.NullPointerException If the bundleFile is null.
     * @return The installed service.
     */
    public static Object deployBundle(File bundleFile) throws IllegalArgumentException, NullPointerException, IOException {
        if(bundleFile == null) throw new NullPointerException("The bundleFile to deploy is null");
        if(!bundleFile.exists()) throw new FileNotFoundException("The bundleFile does not exist");
        if(!bundleFile.isFile()) throw new IllegalArgumentException("The bundleFile has to be a file");

        if(!bundleFile.getParentFile().toPath().normalize().equals(pluginsDirectory.toPath())) {
            Files.copy(bundleFile.toPath(), pluginsDirectory.toPath().resolve(bundleFile.getName()), StandardCopyOption.REPLACE_EXISTING);
        }

        Bundle bundle = null;
        try {
            bundle = osgiFramework.getBundleContext()
                    .installBundle(String.format("file:%1$s/%2$s", pluginsDirectory.getAbsolutePath(), bundleFile.getName()));
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

            if(serviceReference instanceof IMarkup) {
                service = markupServiceTracker.getService(serviceReference);
            } else if(serviceReference instanceof IContentExtension) {
                service = contentExtensionServiceTracker.getService(serviceReference);
            }
        }

        return service;
    }

    /**
     * Return the list of installed services which are from the given class.
     * @param serviceType
     * @param <T>
     * @return the list of installed services or an empty list if there is no service corresponding to the given class.
     */
    public static <T> List<T> getInstalledServices(Class<T> serviceType) {
        List<T> services = new ArrayList<>();

        Object[] allServices = null;

        if(IMarkup.class.isAssignableFrom(serviceType)) {
            allServices = markupServiceTracker.getServices();
        } else if(IContentExtension.class.isAssignableFrom(serviceType)) {
            allServices = contentExtensionServiceTracker.getServices();
        } else if(IHostingConnector.class.isAssignableFrom(serviceType)) {
            allServices = hostingConnectorServiceTracker.getServices();
        }

        if(allServices != null && allServices.length > 0) {
            services = Arrays.stream(allServices)
                    .filter(service -> serviceType.isAssignableFrom(service.getClass()))
                    .map(service -> (T) service)
                    .collect(Collectors.toList());
        }

        return services;
    }
}
