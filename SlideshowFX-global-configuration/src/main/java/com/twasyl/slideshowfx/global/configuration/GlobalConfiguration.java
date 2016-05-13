package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.logs.SlideshowFXHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import static java.nio.charset.StandardCharsets.UTF_8;

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
     * Name of the parameter used to specify if auto saving files is enabled. The value of the parameter is a boolean.
     */
    private static final String AUTO_SAVING_ENABLED_PARAMETER = "application.autoSaving.enabled";

    /**
     * Name of the parameter used to specify the interval for auto saving files. The value of this parameter must be
     * given in seconds.
     */
    private static final String AUTO_SAVING_INTERVAL_PARAMETER = "application.autoSaving.interval";

    /**
     * Name of the parameter used to specify whether the temporary files are deleted when the application is exiting.
     * The value of the parameter is a boolean.
     */
    private static final String TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER = "application.temporaryFiles.deleteOnExit";

    /**
     * Name of the parameter used to specify how old can temporary files be before being deleted when exiting the
     * application. The value of this parameter must be given in seconds.
     */
    private static final String TEMPORARY_FILES_MAX_AGE_PARAMETER = "application.temporaryFiles.maxAge";

    /**
     * The default {@link Charset} used by the application when writing files, readings files and converting strings.
     */
    private static final Charset DEFAULT_CHARSET = UTF_8;

    /**
     * Name of the parameter for defining the log level.
     */
    private static final String LOG_LEVEL_PARAMETER = ".level";

    /**
     * Name of the parameter for specifying the log handler.
     */
    private static final String LOG_HANDLERS_PARAMETER = "handlers";

    /**
     * Name of the parameter suffix for the specifying the encoding of the log file.
     */
    private static final String LOG_ENCODING_SUFFIX = ".encoding";

    /**
     * Name of the parameter for specifying the file log limit.
     */
    private static final String LOG_FILE_LIMIT_PARAMETER = "java.util.logging.FileHandler.limit";

    /**
     * Name of the parameter for specifying the pattern of the log file name.
     */
    private static final String LOG_FILE_PATTERN_PARAMETER = "java.util.logging.FileHandler.pattern";

    /**
     * Name of the parameter suffix for the log file formatter.
     */
    private static final String LOG_FORMATTER_SUFFIX = ".formatter";

    /**
     * Name of the parameter for specifying if logs must be appended to the log file.
     */
    private static final String LOG_FILE_APPEND_PARAMETER = "java.util.logging.FileHandler.append";

    /**
     * Creates the configuration directory represented by the {@link #APPLICATION_DIRECTORY} variable if it doesn't
     * already exist.
     * @return {@code true} if the application directory has been created by this method, {@code false} otherwise.
     */
    public synchronized static boolean createApplicationDirectory() {
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
    public synchronized static boolean createConfigurationFile() {
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
     * Fill the configuration file with default values if it exists.
     */
    public synchronized static void fillConfigurationWithDefaultValue() {
        if(CONFIG_FILE.exists()) {
            final Properties properties = readAllPropertiesFromConfigurationFile();

            if(!properties.containsKey(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER)) enableTemporaryFilesDeletionOnExit(true);
            if(!properties.containsKey(TEMPORARY_FILES_MAX_AGE_PARAMETER)) setTemporaryFilesMaxAge(7);
            if(!properties.containsKey(AUTO_SAVING_ENABLED_PARAMETER)) enableAutoSaving(false);
            if(!properties.containsKey(AUTO_SAVING_INTERVAL_PARAMETER)) setAutoSavingInterval(5);
            if(!properties.containsKey(LOG_LEVEL_PARAMETER)) setLogLevel(Level.INFO);
            if(!properties.containsKey(LOG_HANDLERS_PARAMETER)) setLogHandler(FileHandler.class, SlideshowFXHandler.class);
            if(!properties.containsKey(LOG_FILE_APPEND_PARAMETER)) setLogFileAppend(true);
            if(!properties.containsKey(FileHandler.class.getName().concat(LOG_ENCODING_SUFFIX))) setLogEncoding(FileHandler.class, UTF_8);
            if(!properties.containsKey(FileHandler.class.getName().concat(LOG_FORMATTER_SUFFIX))) setLogFormatter(FileHandler.class, SimpleFormatter.class);
            if(!properties.containsKey(LOG_FILE_LIMIT_PARAMETER)) setLogFileLimit(50000);
            if(!properties.containsKey(LOG_FILE_PATTERN_PARAMETER)) setLogFilePattern("%h/.SlideshowFX/sfx%g.log");
            if(!properties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_ENCODING_SUFFIX))) setLogEncoding(SlideshowFXHandler.class, UTF_8);
            if(!properties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_FORMATTER_SUFFIX))) setLogFormatter(SlideshowFXHandler.class, SimpleFormatter.class);
        }
    }

    /**
     * Check if the temporary files can be deleted or not. Temporary files can be deleted if the value of the parameter
     * {@link #TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER} is not {@code null] and {@code true} and the value of the
     * parameter {@link #TEMPORARY_FILES_MAX_AGE_PARAMETER} is not {@code null}.
     * @return {@code true} if the temporary files can be deleted, {@code false} otherwise.
     */
    public static boolean canDeleteTemporaryFiles() {
        final Boolean deleteTemporaryFilesOnExist = getBooleanProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER);
        final Long maxAge = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);

        return deleteTemporaryFilesOnExist != null && deleteTemporaryFilesOnExist && maxAge != null;
    }

    /**
     * Read all properties stored in the configuration file. If no properties are found or if the configuration file
     * doesn't exist, an empty object is returned.
     * @return The properties stored in the configuration file.
     */
    private synchronized static Properties readAllPropertiesFromConfigurationFile() {
        final Properties properties = new Properties();

        if(CONFIG_FILE.exists()) {

            try(final Reader reader = new FileReader(CONFIG_FILE)) {
                properties.load(reader);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file", e);
            }
        }

        return properties;
    }

    /**
     * Writes all properties to the configuration file. If the given properties are null, nothing is performed.
     * @param properties The properties to write to the configuration file.
     */
    private synchronized static void writeAllPropertiesToConfigurationFile(final Properties properties) {
        if(properties != null) {
            try (final Writer writer = new FileWriter(CONFIG_FILE)) {
                properties.store(writer, "");
                writer.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not save configuration", e);
            }
        }
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
    public synchronized static String getProperty(final String propertyName) {
        checkPropertyName(propertyName);

        String value = null;

        if(CONFIG_FILE.exists()) {
            final Properties properties = readAllPropertiesFromConfigurationFile();
            value = properties.getProperty(propertyName.trim());
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
    public synchronized static void setProperty(final String propertyName, final String propertyValue) {
        checkPropertyName(propertyName);
        checkPropertyValue(propertyValue);

        final Properties properties = readAllPropertiesFromConfigurationFile();
        properties.put(propertyName.trim(), propertyValue);
        writeAllPropertiesToConfigurationFile(properties);
    }

    /**
     * Remove a property from the configuration file. If the property doesn't exist, nothing is performed.
     * @param propertyName The name of the property to remove.
     */
    public synchronized static void removeProperty(final String propertyName) {
        checkPropertyName(propertyName);

        final Properties properties = readAllPropertiesFromConfigurationFile();

        if(properties.containsKey(propertyName.trim())) {
            properties.remove(propertyName.trim());
            writeAllPropertiesToConfigurationFile(properties);
        }
    }

    /**
     * Check if the given property name is valid or not. The property name is considered valid if if is not {@code null}
     * and its value is not empty.
     * @param propertyName The name of the property to check.
     * @throws java.lang.NullPointerException If the property name is {@code null}.
     * @throws java.lang.IllegalArgumentException If the property name is empty.
     */
    private static void checkPropertyName(final String propertyName) {
        if(propertyName == null) throw new NullPointerException("The property name can not be null");
        if(propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");
    }

    /**
     * Check if the given property name is valid or not. The property name is considered valid if if is not {@code null}
     * and its value is not empty.
     * @param propertyValue The value to check.
     * @throws java.lang.NullPointerException If the property value is {@code null}.
     * @throws java.lang.IllegalArgumentException If the property value is empty.
     */
    private static void checkPropertyValue(final String propertyValue) {
        if(propertyValue == null) throw new NullPointerException("The property value can not be null");
        if(propertyValue.trim().isEmpty()) throw new IllegalArgumentException("The property value can not be empty");
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
     * Check if the auto saving is enabled on exit.
     * @return {@code true} if the auto saving is enabled, {@code false} otherwise.
     */
    public static boolean isAutoSavingEnabled() {
        final Boolean autoSave = getBooleanProperty(AUTO_SAVING_ENABLED_PARAMETER);
        return autoSave == null ? Boolean.FALSE : autoSave;
    }

    /**
     * Enable or disable the auto saving configuration..
     * @param enabled The value of the parameter.
     */
    public static void enableAutoSaving(final boolean enabled) {
        setProperty(AUTO_SAVING_ENABLED_PARAMETER, String.valueOf(enabled));
    }

    /**
     * Get the interval for auto saving files.
     * @return The interval in minutes.
     */
    public static Long getAutoSavingInterval() {
        final Long intervalInSeconds = getLongProperty(AUTO_SAVING_INTERVAL_PARAMETER);
        return intervalInSeconds == null ? null : TimeUnit.SECONDS.toMinutes(intervalInSeconds);
    }

    /**
     * Set the auto saving interval configuration parameter.
     * @param intervalInMinutes The interval in minutes for the auto saving parameter.
     */
    public static void setAutoSavingInterval(final long intervalInMinutes) {
        setProperty(AUTO_SAVING_INTERVAL_PARAMETER, String.valueOf(TimeUnit.MINUTES.toSeconds(intervalInMinutes)));
    }

    /**
     * Removes the auto saving interval from the configuration.
     */
    public static void removeAutoSavingInterval() {
        removeProperty(AUTO_SAVING_INTERVAL_PARAMETER);
    }

    /**
     * Check if the temporary files deletion is enabled on exit.
     * @return {@code true} if the deletion is enabled, {@code false} otherwise.
     */
    public static boolean isTemporaryFilesDeletionOnExitEnabled() {
        final Boolean deleteTemporaryFiles = getBooleanProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER);
        return deleteTemporaryFiles == null ? false : deleteTemporaryFiles;
    }

    /**
     * Sets the default log level of the application.
     * @param level The desired log level.
     */
    public static void setLogLevel(final Level level) {
        setProperty(LOG_LEVEL_PARAMETER, level.getName());
    }

    /**
     * Sets the default log handlers.
     * @param handlers The handlers of logs.
     */
    public static void setLogHandler(final Class<? extends Handler> ... handlers) {
        final StringJoiner joiner = new StringJoiner(" ");
        Arrays.stream(handlers).forEach(handler -> joiner.add(handler.getName()));

        setProperty(LOG_HANDLERS_PARAMETER, joiner.toString());
    }

    /**
     * Sets the encoding of log files.
     * @param handler The class handler to set the encoding for.
     * @param charset The encoding of log files.
     */
    public static void setLogEncoding(final Class<? extends Handler> handler, final Charset charset) {
        setProperty(handler.getName().concat(LOG_ENCODING_SUFFIX), charset.displayName());
    }

    /**
     * Sets the size in bytes of the log files.
     * @param size The size, in bytes, of log files.
     */
    public static void setLogFileLimit(final long size) {
        setProperty(LOG_FILE_LIMIT_PARAMETER, String.valueOf(size));
    }

    /**
     * Sets the log files pattern.
     * @param pattern The pattern of log files.
     */
    public static void setLogFilePattern(final String pattern) {
        setProperty(LOG_FILE_PATTERN_PARAMETER, pattern);
    }

    /**
     * Sets the class responsible of formatting log files.
     * @param handler The class handler to set the formatter for.
     * @param formatter The formatter to use for log files.
     */
    public static void setLogFormatter(final Class<? extends Handler> handler, final Class<? extends Formatter> formatter) {
        setProperty(handler.getName().concat(LOG_FORMATTER_SUFFIX), formatter.getName());
    }

    /**
     * Defines if the logs should be append or not to the log files.
     * @param append {@code true} to allow appending, {@code false} otherwise.
     */
    public static void setLogFileAppend(final boolean append) {
        setProperty(LOG_FILE_APPEND_PARAMETER, String.valueOf(append));
    }

    /**
     * Enable or disable the temporary files deletion.
     * @param enable {@code true} to enable the deletion, {@code false} otherwise.
     */
    public static void enableTemporaryFilesDeletionOnExit(final boolean enable) {
        setProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER, String.valueOf(enable));
    }

    /**
     * Get the temporary files max age parameter's value.
     * @return The max age of temporary files in days.
     */
    public static Long getTemporaryFilesMaxAge() {
        final Long ageInSeconds = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);
        return ageInSeconds == null ? null : TimeUnit.SECONDS.toDays(ageInSeconds);
    }

    /**
     * Set the max age of temporary files before they are deleted.
     * @param maxAgeInDays The max age of the temporary files.
     */
    public static void setTemporaryFilesMaxAge(final long maxAgeInDays) {
        setProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER, String.valueOf(TimeUnit.DAYS.toSeconds(maxAgeInDays)));
    }

    /**
     * Remove the temporary files max age from the configuration.
     */
    public static void removeTemporaryFilesMaxAge() {
        removeProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);
    }

    /**
     * Get the default {@link Charset} used by the application.
     * @return The default charset used by the application.
     */
    public static Charset getDefaultCharset() { return DEFAULT_CHARSET; }
}
