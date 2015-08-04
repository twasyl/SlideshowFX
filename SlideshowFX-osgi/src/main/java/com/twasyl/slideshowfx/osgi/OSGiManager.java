/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        configurationMap.put("org.osgi.framework.bootdelegation", "com.twasyl.slideshowfx.markup," +
                "com.twasyl.slideshowfx.content.extension," +
                "com.twasyl.slideshowfx.hosting.connector," +
                "com.twasyl.slideshowfx.hosting.connector.io," +
                "com.twasyl.slideshowfx.hosting.connector.exceptions," +
                "com.twasyl.slideshowfx.snippet.executor," +
                "com.twasyl.slideshowfx.osgi," +
                "com.twasyl.slideshowfx.engine.*," +
                "com.twasyl.slideshowfx.global.configuration," +
                "com.twasyl.slideshowfx.utils.*," +
                "com.twasyl.slideshowfx.plugin," +
                "de.jensd.fx.glyphs," +
                "de.jensd.fx.glyphs.fontawesome," +
                "sun.misc," +
                "org.w3c.*," +
                "javax.*," +
                "javafx.*," +
                "com.sun.javafx");
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
        if(!GlobalConfiguration.PLUGINS_DIRECTORY.exists()) GlobalConfiguration.PLUGINS_DIRECTORY.mkdirs();

        Arrays.stream(GlobalConfiguration.PLUGINS_DIRECTORY.listFiles((dir, name) -> name.endsWith(".jar")))
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
     * @throws IllegalArgumentException If the bundleFile is not a directory.
     * @throws java.io.FileNotFoundException If the bundleFile is not found.
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
            e.printStackTrace();
        }

        return services;
    }

    public static final String PRESENTATION_FOLDER = "presentation.folder";
    public static final String PRESENTATION_RESOURCES_FOLDER = "presentation.resources.folder";

    public static Object getPresentationProperty(String property) {
        Object value = null;

        try {
            // Class and methods for PresentationDAO
            final Class presentationDaoClass = Class.forName("com.twasyl.slideshowfx.dao.PresentationDAO");
            final Method getInstanceMethod = presentationDaoClass.getMethod("getInstance");
            final Method getCurrentPresentationMethod = presentationDaoClass.getMethod("getCurrentPresentation");

            // Class and methods for AbstractEngine
            final Class abstractEngineClass = Class.forName("com.twasyl.slideshowfx.engine.AbstractEngine");
            final Method getWorkingDirectoryMethod = abstractEngineClass.getMethod("getWorkingDirectory");

            // Class and methods for PresentationEngine
            final Class presentationEngineClass = Class.forName("com.twasyl.slideshowfx.engine.presentation.PresentationEngine");
            final Method getTemplateConfigurationMethod = presentationEngineClass.getMethod("getTemplateConfiguration");

            // Class and methods for TemplateConfiguration
            final Class templateConfigurationClass = Class.forName("com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration");
            final Method getResourcesDirectoryMethod = templateConfigurationClass.getMethod("getResourcesDirectory");

            final Object presentationDaoInstance = getInstanceMethod.invoke(presentationDaoClass);
            final Object currentPresentation = getCurrentPresentationMethod.invoke(presentationDaoInstance);

            if(currentPresentation != null) {
                if(PRESENTATION_FOLDER.equals(property)) value = getWorkingDirectoryMethod.invoke(currentPresentation);
                else if(PRESENTATION_RESOURCES_FOLDER.equals(property)) {
                    final Object templateConfiguration = getTemplateConfigurationMethod.invoke(currentPresentation);

                    if(templateConfiguration != null) {
                        value = getResourcesDirectoryMethod.invoke(templateConfiguration);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        /*if(PRESENTATION_FOLDER.equals(property) && PresentationDAO.getInstance().getCurrentPresentation() != null) {
            value = PresentationDAO.getInstance().getCurrentPresentation().getWorkingDirectory();
        } else if(PRESENTATION_RESOURCES_FOLDER.equals(property)
                && PresentationDAO.getInstance().getCurrentPresentation() != null
                && PresentationDAO.getInstance().getCurrentPresentation().getTemplateConfiguration() != null) {
            value = PresentationDAO.getInstance().getCurrentPresentation().getTemplateConfiguration().getResourcesDirectory();
        }      */

        return value;
    }
}
