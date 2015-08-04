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

package com.twasyl.slideshowfx.global.configuration;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides methods for accessing configuration properties.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(GlobalConfiguration.class.getName());

    public static final File APPLICATION_DIRECTORY = new File(System.getProperty("user.home"), ".SlideshowFX");
    public static final File CONFIG_FILE = new File(APPLICATION_DIRECTORY, ".slideshowfx.configuration.properties");
    public static final File PLUGINS_DIRECTORY = new File(APPLICATION_DIRECTORY, "plugins");

    /**
     * Get a property from the configuration. This methods return {@code null} is the property
     * is not found or if the configuration file does not exist.
     *
     * @param propertyName The name of the property to retrieve.
     * @return The value of the property or {@code null} if it is not found or the configuration does not exist.
     * @throws java.lang.NullPointerException     If the property name is null.
     * @throws java.lang.IllegalArgumentException If the property name is empty.
     */
    public final static String getProperty(final String propertyName) {
        if(propertyName == null) throw new NullPointerException("The property name can not be null");
        if(propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");

        String value = null;

        if(CONFIG_FILE.exists()) {
            final Properties properties = new Properties();

            try(final Reader reader = new FileReader(CONFIG_FILE)) {
                properties.load(reader);
                value = properties.getProperty(propertyName.trim());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file", e);
            }
        }

        return value;
    }

    /**
     * Save the given {@code propertyName} and {@code propertyValue} to the configuration.
     *
     * @param propertyName The name of the property to save.
     * @param propertyValue The value of the property to save.
     * @throws java.lang.NullPointerException If the name or value of the property is null.
     * @throws java.lang.IllegalArgumentException If the name or value of the property is empty.
     */
    public final static void setProperty(final String propertyName, final String propertyValue) {
        if(propertyName == null) throw new NullPointerException("The property name can not be null");
        if(propertyValue == null) throw new NullPointerException("The property value can not be null");
        if(propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");
        if(propertyValue.trim().isEmpty()) throw new IllegalArgumentException("The property value can not be empty");

        final Properties properties = new Properties();

        // Load the current properties if they exist
        if(CONFIG_FILE.exists()) {
            try(final Reader reader = new FileReader(CONFIG_FILE)) {
                properties.load(reader);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file", e);
            }
        }

        // Add the property
        properties.put(propertyName.trim(), propertyValue);

        // Store everything
        try(final Writer writer = new FileWriter(CONFIG_FILE)) {
            properties.store(writer, "");
            writer.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not save configuration", e);
        }
    }
}
