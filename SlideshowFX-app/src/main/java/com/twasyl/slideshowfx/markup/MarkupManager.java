/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.markup;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class MarkupManager {
    private static final Logger LOGGER = Logger.getLogger(MarkupManager.class.getName());

    private static final File pluginsDirectory = new File(System.getProperty("user.home") + "/.SlideshowFX/plugins");
    private static Framework osgiFramework;
    private static ServiceTracker serviceTracker;

    public static void start() {

        Map configurationMap = new HashMap<>();
        configurationMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, IMarkup.class.getPackage().getName() + "; 1.0.0");
        configurationMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
        configurationMap.put("org.osgi.osgiFramework.storage.clean", "onFirstInit");
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
            serviceTracker = new ServiceTracker(
                    osgiFramework.getBundleContext(),
                    osgiFramework.getBundleContext().createFilter("(objectClass=" + IMarkup.class.getName() + ")"),
                    null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
        serviceTracker.open();

        // Deploy initially present plugins
        if(!pluginsDirectory.exists()) pluginsDirectory.mkdirs();
        
        Arrays.stream(pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar")))
              .forEach(file -> {
                  Bundle bundle = null;
                  try {
                      bundle = osgiFramework.getBundleContext().installBundle(String.format("file:%1$s/%2$s", pluginsDirectory.getAbsolutePath(), file.getName()));
                  } catch (BundleException e) {
                      LOGGER.log(Level.WARNING, "Can not install bundle", e);
                  }
                  if(bundle != null) {
                      try {
                          bundle.start();
                      } catch (BundleException e) {
                          LOGGER.log(Level.WARNING, "Can not install bundle", e);
                      }
                  }
              });
    }

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

    public static String convertToHtml(String code, String markupValue) {
        String html = null;

        Object[] services = serviceTracker.getServices();

        if(services != null) {
            IMarkup markup = (IMarkup) Arrays.stream(services)
            .filter(service -> ((IMarkup) service).getCode().equals(code))
            .findFirst()
            .get();

            if(markup != null) {
                html = markup.convertAsHtml(markupValue);
            } else {
                LOGGER.log(Level.SEVERE, "Can not find IMarkup for code " + code);
            }
        }
        return html;
    }

    public static List<IMarkup> getInstalledMarkupSyntax() {
        List<IMarkup> markups = new ArrayList<>();
        Object[] services = serviceTracker.getServices();

        if(services != null && services.length > 0) {
            Arrays.stream(services).forEach(service -> markups.add((IMarkup) service));
        }

        return markups;
    }

    /**
     * Test if the given <code>contentCode</code> is supported.
     * @param contentCode
     * @return
     */
    public static boolean isContentSupported(final String contentCode) {
        boolean supported = false;

        Object[] services = serviceTracker.getServices();

        if(services != null) {
            Optional<IMarkup> iMarkup =  Arrays.stream(services)
                                                  .filter(service -> service instanceof IMarkup)
                                                  .map(service -> (IMarkup) service)
                                                  .filter(service -> contentCode.equals(service.getCode()))
                                                  .findFirst();

            supported = iMarkup.isPresent();
        }

        return supported;
    }
}
