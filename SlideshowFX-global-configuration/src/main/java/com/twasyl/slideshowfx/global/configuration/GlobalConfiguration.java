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

package com.twasyl.slideshowfx.global.configuration;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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
     * Name of the parameter used to specify whether the temporary files are deleted when the application is exiting.
     * The value of the parameter is a boolean.
     */
    public static final String DELETE_TEMPORARY_FILES_ON_EXIT_PARAMETER = "application.temporaryFiles.deleteOnExit";

    /**
     * Name of the parameter used to specify how old can temporary files be before being deleted when exiting the
     * application. The value of this parameter must be given in seconds.
     */
    public static final String TEMPORARY_FILES_MAX_AGE_PARAMETER = "application.temporaryFiles.maxAge";

    /**
     * Creates the configuration directory represented by the {@link #APPLICATION_DIRECTORY} variable if it doesn't
     * already exist.
     * @return {@code true} if the application directory has been created by this method, {@code false} otherwise.
     */
    public static boolean createApplicationDirectory() {
        boolean created = false;

        if(!APPLICATION_DIRECTORY.exists()) {
            created = APPLICATION_DIRECTORY.mkdir();
        }

        return created;
    }

    /**
     * Creates the configuration file of the application, represented by the {@link #CONFIG_FILE}
     * variable if it doesn't already exist.
     * @return {@code true} if the configuration file has been created by this method, {@code false} otherwise.
     */
    public static boolean createConfigurationFile() {
        boolean created = false;

        if(!CONFIG_FILE.exists()) {
            try {
                created = CONFIG_FILE.createNewFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not create the configuration file", e);
            }
        }

        return created;
    }

    /**
     * Fill the configuration with default values if it exists.
     */
    public static void fillConfigurationWithDefaultValue() {
        if(CONFIG_FILE.exists()) {
            setProperty(DELETE_TEMPORARY_FILES_ON_EXIT_PARAMETER, "true");
            setProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER, String.valueOf(TimeUnit.DAYS.toSeconds(7)));
        }
    }

    /**
     * Check if the temporary files can be deleted or not. Temporary files can be deleted if the value of the parameter
     * {@link #DELETE_TEMPORARY_FILES_ON_EXIT_PARAMETER} is not {@code null] and {@code true} and the value of the
     * parameter {@link #TEMPORARY_FILES_MAX_AGE_PARAMETER} is not {@code null}.
     * @return {@code true} if the temporary files can be deleted, {@code false} otherwise.
     */
    public static boolean canDeleteTemporaryFiles() {
        final Boolean deleteTemporaryFilesOnExist = getBooleanProperty(DELETE_TEMPORARY_FILES_ON_EXIT_PARAMETER);
        final Long maxAge = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);

        return deleteTemporaryFilesOnExist != null && deleteTemporaryFilesOnExist && maxAge != null;
    }

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

    /**
     * Get the value of a property as a {@link Long}.
     * @param propertyName The name of the property to get.
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Long getLongProperty(final String propertyName) {
        Long value = null;

        final String retrievedProperty = getProperty(propertyName);
        if(retrievedProperty != null) {
            try {
                value = Long.parseLong(retrievedProperty);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "The value of the property '" + propertyName + "' can not be parsed", ex);
            }
        }

        return value;
    }

    /**
     * Get the value of a property as a {@link Boolean}.
     * @param propertyName The name of the property to get.
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Boolean getBooleanProperty(final String propertyName) {
        Boolean value = null;

        final String retrievedProperty = getProperty(propertyName);
        if(retrievedProperty != null) {
            try {
                value = Boolean.parseBoolean(retrievedProperty);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "The value of the property '" + propertyName + "' can not be parsed", ex);
            }
        }

        return value;
    }

    /**
     * Get the temporary files max age parameter's value.
     * @return The max age of temporary files in days.
     */
    public static Long getTemporaryFilesMaxAge() {
        final Long ageInSeconds = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);
        return TimeUnit.SECONDS.toDays(ageInSeconds);
    }
}
