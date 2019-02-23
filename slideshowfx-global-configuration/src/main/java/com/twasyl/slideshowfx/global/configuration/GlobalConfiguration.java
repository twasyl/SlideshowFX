package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.logs.SlideshowFXHandler;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;

/**
 * This class provides methods for accessing configuration properties.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class GlobalConfiguration {
    private static final Logger LOGGER = Logger.getLogger(GlobalConfiguration.class.getName());

    protected static final String APPLICATION_DIRECTORY_PROPERTY = "application.dir";
    protected static final String PLUGINS_DIRECTORY_PROPERTY = "plugins.dir";
    protected static final String TEMPLATE_LIBRARY_DIRECTORY_PROPERTY = "templateLib.dir";

    protected static final String DEFAULT_APPLICATION_DIRECTORY_NAME = ".SlideshowFX";
    protected static final String DEFAULT_PLUGINS_DIRECTORY_NAME = "plugins";
    protected static final String DEFAULT_TEMPLATE_LIBRARY_DIRECTORY_NAME = "templateLibrary";
    protected static final String SLIDESHOWFX_CONFIGURATION_FILE = ".slideshowfx.configuration.properties";
    protected static final String SLIDESHOWFX_CONTEXT_FILE_NAME = ".slideshowfx.context.xml";
    protected static final Long DEFAULT_MAX_RECENT_PRESENTATIONS = 10L;
    protected static final Long DEFAULT_SNAPSHOT_DELAY = 500L;
    protected static final String DEFAULT_THEME_NAME = "Dark";

    private static File APPLICATION_DIRECTORY = null;
    private static File PLUGINS_DIRECTORY = null;
    private static File TEMPLATE_LIBRARY_DIRECTORY = null;
    private static File CONFIG_FILE = null;
    private static File LOGGING_CONFIG_FILE = null;
    private static Set<RecentPresentation> RECENT_PRESENTATIONS = null;
    private static final Object RECENT_PRESENTATIONS_LOCK = new Object();

    private static final GlobalConfigurationObservable OBSERVABLE = new GlobalConfigurationObservable();

    /**
     * Name of the parameter used to specify if auto saving files is enabled. The value of the parameter is a boolean.
     */
    protected static final String AUTO_SAVING_ENABLED_PARAMETER = "application.autoSaving.enabled";

    /**
     * Name of the parameter used to specify the interval for auto saving files. The value of this parameter must be
     * given in seconds.
     */
    protected static final String AUTO_SAVING_INTERVAL_PARAMETER = "application.autoSaving.interval";

    /**
     * Name of the parameter used to specify whether the temporary files are deleted when the application is exiting.
     * The value of the parameter is a boolean.
     */
    protected static final String TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER = "application.temporaryFiles.deleteOnExit";

    /**
     * Name of the parameter used to specify how old can temporary files be before being deleted when exiting the
     * application. The value of this parameter must be given in seconds.
     */
    protected static final String TEMPORARY_FILES_MAX_AGE_PARAMETER = "application.temporaryFiles.maxAge";

    /**
     * Name of the parameter used to specify how many open presentations are stored and displayed in the "Open recent"
     * menu of the application.
     */
    protected static final String MAX_RECENT_PRESENTATIONS = "application.max.recentpresentations";

    /**
     * Name of the parameter used to specify the delay before taking a snapshot of a browser.
     */
    protected static final String SNAPSHOT_DELAY = "application.snapshot.delay";

    /**
     * Name of the parameter used to specify the theme of the application.
     */
    protected static final String THEME = "application.theme";


    /**
     * Name of the parameter used to specify the Twitter consumer key.
     */
    protected static final String TWITTER_CONSUMER_KEY = "service.twitter.consumerKey";

    /**
     * Name of the parameter used to specify the Twitter consumer secret.
     */
    protected static final String TWITTER_CONSUMER_SECRET = "service.twitter.consumerSecret";

    /**
     * The default {@link Charset} used by the application when writing files, readings files and converting strings.
     */
    protected static final Charset DEFAULT_CHARSET = UTF_8;


    /**
     * Name of the parameter for defining the log level.
     */
    protected static final String LOG_LEVEL_PARAMETER = ".level";

    /**
     * Name of the parameter for specifying the log handler.
     */
    protected static final String LOG_HANDLERS_PARAMETER = "handlers";

    /**
     * Name of the parameter suffix for the specifying the encoding of the log file.
     */
    protected static final String LOG_ENCODING_SUFFIX = ".encoding";

    /**
     * Name of the parameter for specifying the file log limit.
     */
    protected static final String LOG_FILE_LIMIT_PARAMETER = "java.util.logging.FileHandler.limit";

    /**
     * Name of the parameter for specifying the pattern of the log file name.
     */
    protected static final String LOG_FILE_PATTERN_PARAMETER = "java.util.logging.FileHandler.pattern";

    /**
     * Name of the parameter suffix for the log file formatter.
     */
    protected static final String LOG_FORMATTER_SUFFIX = ".formatter";

    /**
     * Name of the parameter for specifying if logs must be appended to the log file.
     */
    protected static final String LOG_FILE_APPEND_PARAMETER = "java.util.logging.FileHandler.append";

    public static void addObserver(final GlobalConfigurationObserver observer) {
        OBSERVABLE.addObserver(observer);
    }

    private GlobalConfiguration() {
    }

    /**
     * Get the application directory used to store the plugins and the configuration. The method will determine the
     * directory by checking if there is a system property named {@value #APPLICATION_DIRECTORY_PROPERTY} that defines
     * the directory to use and if not, the directory will be the {@value #DEFAULT_APPLICATION_DIRECTORY_NAME} directory
     * stored in the user's home.
     *
     * @return The application directory.
     */
    public static synchronized File getApplicationDirectory() {
        if (APPLICATION_DIRECTORY == null) {
            final Properties properties = System.getProperties();

            if (properties.containsKey(APPLICATION_DIRECTORY_PROPERTY)) {
                APPLICATION_DIRECTORY = new File(properties.getProperty(APPLICATION_DIRECTORY_PROPERTY));
            } else {
                APPLICATION_DIRECTORY = new File(System.getProperty("user.home"), DEFAULT_APPLICATION_DIRECTORY_NAME);
            }
        }

        return APPLICATION_DIRECTORY;
    }

    /**
     * Get the plugins directory used to store the installed plugins. The method will determine the
     * directory by checking if there is a system property named {@value #PLUGINS_DIRECTORY_PROPERTY} that defines
     * the directory to use and if not, the directory will be the {@value #DEFAULT_PLUGINS_DIRECTORY_NAME} directory
     * stored in the application directory returned by {@link #getApplicationDirectory()}.
     *
     * @return The plugins directory.
     */
    public static synchronized File getPluginsDirectory() {
        if (PLUGINS_DIRECTORY == null) {
            final Properties properties = System.getProperties();

            if (properties.containsKey(PLUGINS_DIRECTORY_PROPERTY)) {
                PLUGINS_DIRECTORY = new File(properties.getProperty(PLUGINS_DIRECTORY_PROPERTY));
            } else {
                PLUGINS_DIRECTORY = new File(getApplicationDirectory(), DEFAULT_PLUGINS_DIRECTORY_NAME);
            }
        }
        return PLUGINS_DIRECTORY;
    }

    /**
     * Get the directory used to store the template's library. The method will determine the
     * directory by checking if there is a system property named {@value #TEMPLATE_LIBRARY_DIRECTORY_PROPERTY} that
     * defines the directory to use and if not, the directory will be the {@value #DEFAULT_TEMPLATE_LIBRARY_DIRECTORY_NAME}
     * directory stored in the application directory returned by {@link #getApplicationDirectory()}.
     *
     * @return The template's library directory.
     */
    public static synchronized File getTemplateLibraryDirectory() {
        if (TEMPLATE_LIBRARY_DIRECTORY == null) {
            final Properties properties = System.getProperties();

            if (properties.containsKey(TEMPLATE_LIBRARY_DIRECTORY_PROPERTY)) {
                TEMPLATE_LIBRARY_DIRECTORY = new File(properties.getProperty(TEMPLATE_LIBRARY_DIRECTORY_PROPERTY));
            } else {
                TEMPLATE_LIBRARY_DIRECTORY = new File(getApplicationDirectory(), DEFAULT_TEMPLATE_LIBRARY_DIRECTORY_NAME);
            }
        }
        return TEMPLATE_LIBRARY_DIRECTORY;
    }

    /**
     * Get the configuration file of the application. The file is named {@code .slideshowfx.configuration.properties}
     * and is stored in the directory returned by {@link #getApplicationDirectory()}.
     *
     * @return The configuration file.
     */
    public static synchronized File getConfigurationFile() {
        if (CONFIG_FILE == null) {
            CONFIG_FILE = new File(getApplicationDirectory(), SLIDESHOWFX_CONFIGURATION_FILE);
        }

        return CONFIG_FILE;
    }

    /**
     * Checks if the configuration returned by the {@link #getConfigurationFile()} exists.
     *
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    public static synchronized boolean configurationFileExists() {
        return getConfigurationFile().exists();
    }

    /**
     * Get the logging configuration file. The method will determine the file by checking if there is a system property
     * named {@code java.util.logging.config.file} that defines the file to use and if not, the file will be the
     * {@code logging.config} file stored in the application directory returned by {@link #getApplicationDirectory()}.
     *
     * @return The logging configuration file.
     */
    public static synchronized File getLoggingConfigFile() {
        if (LOGGING_CONFIG_FILE == null) {
            final Properties properties = System.getProperties();

            if (properties.containsKey("java.util.logging.config.file")) {
                LOGGING_CONFIG_FILE = new File(System.getProperty("java.util.logging.config.file"));
            } else {
                LOGGING_CONFIG_FILE = new File(getApplicationDirectory(), "logging.config");
            }
        }

        return LOGGING_CONFIG_FILE;
    }

    /**
     * Creates the configuration directory represented by the {@link #APPLICATION_DIRECTORY} variable if it doesn't
     * already exist.
     *
     * @return {@code true} if the application directory has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createApplicationDirectory() {
        boolean created = false;

        if (!getApplicationDirectory().exists()) {
            created = getApplicationDirectory().mkdirs();
        }

        return created;
    }

    /**
     * Creates the plugins directory represented by the {@link #PLUGINS_DIRECTORY} variable if it doesn't
     * already exist.
     * If parents directories don't exist, this method will not create them and the directory will not be created.
     *
     * @return {@code true} if the plugins directory has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createPluginsDirectory() {
        boolean created = false;

        if (!getPluginsDirectory().exists()) {
            created = getPluginsDirectory().mkdir();
        }

        return created;
    }

    /**
     * Creates the template library directory represented by the {@link #TEMPLATE_LIBRARY_DIRECTORY} variable if it doesn't
     * already exist.
     * If parents directories don't exist, this method will not create them and the directory will not be created.
     *
     * @return {@code true} if the template library directory has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createTemplateLibraryDirectory() {
        boolean created = false;

        if (!getTemplateLibraryDirectory().exists()) {
            created = getTemplateLibraryDirectory().mkdir();
        }

        return created;
    }

    /**
     * Creates the configuration file of the application, represented by the {@link #getConfigurationFile()}
     * variable if it doesn't already exist.
     *
     * @return {@code true} if the configuration file has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createConfigurationFile() {
        boolean created = false;

        if (!configurationFileExists()) {
            try {
                created = getConfigurationFile().createNewFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not create the configuration file", e);
            }
        }

        return created;
    }

    /**
     * Creates the logging configuration file of the application, represented by the {@link #getLoggingConfigFile()}
     * variable if it doesn't already exist.
     *
     * @return {@code true} if the logging configuration file has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createLoggingConfigurationFile() {
        boolean created = false;

        if (!getLoggingConfigFile().exists()) {
            try {
                created = getLoggingConfigFile().createNewFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not create the logging configuration file", e);
            }
        }

        return created;
    }

    /**
     * Creates the logging configuration file of the application, represented by the given {@code loggingConfigFile}.
     * Calling this method will cause the {@link #getLoggingConfigFile()} method to return the provided file.
     *
     * @return {@code true} if the logging configuration file has been created by this method, {@code false} otherwise.
     */
    public static synchronized boolean createLoggingConfigurationFile(final File loggingConfigFile) {
        boolean created = false;

        if (!loggingConfigFile.exists()) {
            try {
                created = loggingConfigFile.createNewFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not create the logging configuration file", e);
            }
        }

        LOGGING_CONFIG_FILE = loggingConfigFile;

        return created;
    }

    /**
     * Fill the configuration file with default values if it exists.
     */
    public static synchronized void fillConfigurationWithDefaultValue() {
        if (configurationFileExists()) {
            final Properties properties = readAllPropertiesFromConfigurationFile(getConfigurationFile());

            if (!properties.containsKey(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER))
                enableTemporaryFilesDeletionOnExit(true);
            if (!properties.containsKey(TEMPORARY_FILES_MAX_AGE_PARAMETER)) setTemporaryFilesMaxAge(7);
            if (!properties.containsKey(AUTO_SAVING_ENABLED_PARAMETER)) enableAutoSaving(false);
            if (!properties.containsKey(AUTO_SAVING_INTERVAL_PARAMETER)) setAutoSavingInterval(5);
        }
    }

    public static synchronized void fillLoggingConfigurationFileWithDefaultValue() {
        if (getLoggingConfigFile().exists()) {
            final Properties properties = readAllPropertiesFromConfigurationFile(getLoggingConfigFile());

            if (!properties.containsKey(LOG_LEVEL_PARAMETER)) setLogLevel(Level.INFO);
            if (!properties.containsKey(LOG_HANDLERS_PARAMETER))
                setLogHandler(FileHandler.class, SlideshowFXHandler.class);
            if (!properties.containsKey(LOG_FILE_APPEND_PARAMETER)) setLogFileAppend(true);
            if (!properties.containsKey(FileHandler.class.getName().concat(LOG_ENCODING_SUFFIX)))
                setLogEncoding(FileHandler.class, UTF_8);
            if (!properties.containsKey(FileHandler.class.getName().concat(LOG_FORMATTER_SUFFIX)))
                setLogFormatter(FileHandler.class, SimpleFormatter.class);
            if (!properties.containsKey(LOG_FILE_LIMIT_PARAMETER)) setLogFileLimit(50000);
            if (!properties.containsKey(LOG_FILE_PATTERN_PARAMETER)) setLogFilePattern("%h/.SlideshowFX/sfx%g.log");
            if (!properties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_ENCODING_SUFFIX)))
                setLogEncoding(SlideshowFXHandler.class, UTF_8);
            if (!properties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_FORMATTER_SUFFIX)))
                setLogFormatter(SlideshowFXHandler.class, SimpleFormatter.class);
        }
    }

    /**
     * Check if the temporary files can be deleted or not. Temporary files can be deleted if the value of the parameter
     * {@link #TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER} is not {@code null] and {@code true} and the value of the
     * parameter {@link #TEMPORARY_FILES_MAX_AGE_PARAMETER} is not {@code null}.
     *
     * @return {@code true} if the temporary files can be deleted, {@code false} otherwise.
     */
    public static boolean canDeleteTemporaryFiles() {
        final boolean deleteTemporaryFilesOnExist = getBooleanProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER, false);
        final Long maxAge = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);

        return deleteTemporaryFilesOnExist && maxAge != null;
    }

    /**
     * Read all properties stored in the configuration file. If no properties are found or if the configuration file
     * doesn't exist, an empty object is returned.
     *
     * @return The properties stored in the configuration file.
     */
    protected static synchronized Properties readAllPropertiesFromConfigurationFile(final File file) {
        final Properties properties = new Properties();

        if (file.exists()) {

            try (final Reader reader = new FileReader(file)) {
                properties.load(reader);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not load configuration file: " + file.getAbsolutePath(), e);
            }
        }

        return properties;
    }

    /**
     * Writes all properties to the configuration file. If the given properties are null, nothing is performed.
     *
     * @param file       The file in which the properties will be written.
     * @param properties The properties to write to the configuration file.
     */
    private static synchronized void writeAllPropertiesToConfigurationFile(final File file, final Properties properties) {
        if (properties != null) {
            try (final Writer writer = new FileWriter(file)) {
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
     * @throws NullPointerException     If the property name is null.
     * @throws IllegalArgumentException If the property name is empty.
     */
    public static synchronized String getProperty(final String propertyName) {
        return getProperty(getConfigurationFile(), propertyName);
    }

    /**
     * Get a property from the given {@code file}. This methods return {@code null} is the property
     * is not found or if the configuration file does not exist.
     *
     * @param file         The file from which the property will be read.
     * @param propertyName The name of the property to retrieve.
     * @return The value of the property or {@code null} if it is not found or the configuration does not exist.
     * @throws NullPointerException     If the property name is null.
     * @throws IllegalArgumentException If the property name is empty.
     */
    public static synchronized String getProperty(final File file, final String propertyName) {
        checkPropertyName(propertyName);

        String value = null;

        if (file.exists()) {
            final Properties properties = readAllPropertiesFromConfigurationFile(file);
            value = properties.getProperty(propertyName.trim());
        }

        return value;
    }

    /**
     * Save the given {@code propertyName} and {@code propertyValue} to the configuration.
     *
     * @param propertyName  The name of the property to save.
     * @param propertyValue The value of the property to save.
     * @throws NullPointerException     If the name or value of the property is null.
     * @throws IllegalArgumentException If the name or value of the property is empty.
     */
    public static synchronized void setProperty(final String propertyName, final String propertyValue) {
        setProperty(getConfigurationFile(), propertyName, propertyValue);
    }

    /**
     * Save the given {@code propertyName} and {@code propertyValue} to the given {@code file}.
     *
     * @param file          The file in which the property will be set.
     * @param propertyName  The name of the property to save.
     * @param propertyValue The value of the property to save.
     * @throws NullPointerException     If the name or value of the property is null.
     * @throws IllegalArgumentException If the name or value of the property is empty.
     */
    private static synchronized void setProperty(final File file, final String propertyName, final String propertyValue) {
        checkPropertyName(propertyName);
        checkPropertyValue(propertyValue);

        final Properties properties = readAllPropertiesFromConfigurationFile(file);
        properties.put(propertyName.trim(), propertyValue);
        writeAllPropertiesToConfigurationFile(file, properties);
    }

    /**
     * Remove a property from the configuration file. If the property doesn't exist, nothing is performed.
     *
     * @param propertyName The name of the property to remove.
     */
    public static synchronized void removeProperty(final String propertyName) {
        removeProperty(getConfigurationFile(), propertyName);
    }

    /**
     * Remove a property from the configuration file. If the property doesn't exist, nothing is performed.
     *
     * @param file         The file from which the property will be removed.
     * @param propertyName The name of the property to remove.
     */
    public static synchronized void removeProperty(final File file, final String propertyName) {
        checkPropertyName(propertyName);

        final Properties properties = readAllPropertiesFromConfigurationFile(file);

        if (properties.containsKey(propertyName.trim())) {
            properties.remove(propertyName.trim());
            writeAllPropertiesToConfigurationFile(file, properties);
        }
    }

    /**
     * Check if the given property name is valid or not. The property name is considered valid if if is not {@code null}
     * and its value is not empty.
     *
     * @param propertyName The name of the property to check.
     * @throws NullPointerException     If the property name is {@code null}.
     * @throws IllegalArgumentException If the property name is empty.
     */
    private static void checkPropertyName(final String propertyName) {
        if (propertyName == null) throw new NullPointerException("The property name can not be null");
        if (propertyName.trim().isEmpty()) throw new IllegalArgumentException("The property name can not be empty");
    }

    /**
     * Check if the given property name is valid or not. The property name is considered valid if if is not {@code null}
     * and its value is not empty.
     *
     * @param propertyValue The value to check.
     * @throws NullPointerException     If the property value is {@code null}.
     * @throws IllegalArgumentException If the property value is empty.
     */
    private static void checkPropertyValue(final String propertyValue) {
        if (propertyValue == null) throw new NullPointerException("The property value can not be null");
        if (propertyValue.trim().isEmpty()) throw new IllegalArgumentException("The property value can not be empty");
    }

    /**
     * Get the value of a property as a {@link Long}.
     *
     * @param propertyName The name of the property to get.
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Long getLongProperty(final String propertyName) {
        return getLongProperty(getConfigurationFile(), propertyName);
    }

    /**
     * Get the value of a property as a {@link Long}.
     *
     * @param file         The file from which retrieve the property.
     * @param propertyName The name of the property to get.
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Long getLongProperty(final File file, final String propertyName) {
        Long value = null;

        final String retrievedProperty = getProperty(file, propertyName);
        if (retrievedProperty != null) {
            try {
                value = Long.parseLong(retrievedProperty);
            } catch (NumberFormatException ex) {
                LOGGER.log(WARNING, "The value of the property '" + propertyName + "' can not be parsed", ex);
            }
        }

        return value;
    }

    /**
     * Get the value of a property as a {@link Boolean}.
     *
     * @param propertyName The name of the property to get.
     * @param defaultValue
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Boolean getBooleanProperty(final String propertyName, boolean defaultValue) {
        final Boolean property = getBooleanProperty(getConfigurationFile(), propertyName);

        if (property == null) {
            return defaultValue;
        } else {
            return property;
        }
    }

    /**
     * Get the value of a property as a {@link Boolean}.
     *
     * @param file         The file from which retrieve the property.
     * @param propertyName The name of the property to get.
     * @return The value of the property or {@code null} if it is not present or can not be parsed.
     */
    public static Boolean getBooleanProperty(final File file, final String propertyName) {
        Boolean value = null;

        final String retrievedProperty = getProperty(file, propertyName);
        if (retrievedProperty != null) {
            try {
                value = Boolean.parseBoolean(retrievedProperty);
            } catch (NumberFormatException ex) {
                LOGGER.log(WARNING, "The value of the property '" + propertyName + "' can not be parsed", ex);
            }
        }

        return value;
    }

    /**
     * Check if the auto saving is enabled on exit.
     *
     * @return {@code true} if the auto saving is enabled, {@code false} otherwise.
     */
    public static boolean isAutoSavingEnabled() {
        return getBooleanProperty(AUTO_SAVING_ENABLED_PARAMETER, false);
    }

    /**
     * Enable or disable the auto saving configuration..
     *
     * @param enabled The value of the parameter.
     */
    public static void enableAutoSaving(final boolean enabled) {
        setProperty(AUTO_SAVING_ENABLED_PARAMETER, String.valueOf(enabled));
    }

    /**
     * Get the interval for auto saving files.
     *
     * @return The interval in minutes.
     */
    public static Long getAutoSavingInterval() {
        final Long intervalInSeconds = getLongProperty(AUTO_SAVING_INTERVAL_PARAMETER);
        return intervalInSeconds == null ? null : TimeUnit.SECONDS.toMinutes(intervalInSeconds);
    }

    /**
     * Set the auto saving interval configuration parameter.
     *
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
     *
     * @return {@code true} if the deletion is enabled, {@code false} otherwise.
     */
    public static boolean isTemporaryFilesDeletionOnExitEnabled() {
        return getBooleanProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER, false);
    }

    /**
     * Sets the default log level of the application.
     *
     * @param level The desired log level.
     */
    public static void setLogLevel(final Level level) {
        setProperty(getLoggingConfigFile(), LOG_LEVEL_PARAMETER, level.getName());
    }

    /**
     * Sets the default log handlers.
     *
     * @param handlers The handlers of logs.
     */
    public static void setLogHandler(final Class<? extends Handler>... handlers) {
        final StringJoiner joiner = new StringJoiner(" ");
        Arrays.stream(handlers).forEach(handler -> joiner.add(handler.getName()));

        setProperty(getLoggingConfigFile(), LOG_HANDLERS_PARAMETER, joiner.toString());
    }

    /**
     * Sets the encoding of log files.
     *
     * @param handler The class handler to set the encoding for.
     * @param charset The encoding of log files.
     */
    public static void setLogEncoding(final Class<? extends Handler> handler, final Charset charset) {
        setProperty(getLoggingConfigFile(), handler.getName().concat(LOG_ENCODING_SUFFIX), charset.displayName());
    }

    /**
     * Sets the size in bytes of the log files.
     *
     * @param size The size, in bytes, of log files.
     */
    public static void setLogFileLimit(final long size) {
        setProperty(getLoggingConfigFile(), LOG_FILE_LIMIT_PARAMETER, String.valueOf(size));
    }

    /**
     * Sets the log files pattern.
     *
     * @param pattern The pattern of log files.
     */
    public static void setLogFilePattern(final String pattern) {
        setProperty(getLoggingConfigFile(), LOG_FILE_PATTERN_PARAMETER, pattern);
    }

    /**
     * Sets the class responsible of formatting log files.
     *
     * @param handler   The class handler to set the formatter for.
     * @param formatter The formatter to use for log files.
     */
    public static void setLogFormatter(final Class<? extends Handler> handler, final Class<? extends Formatter> formatter) {
        setProperty(getLoggingConfigFile(), handler.getName().concat(LOG_FORMATTER_SUFFIX), formatter.getName());
    }

    /**
     * Defines if the logs should be append or not to the log files.
     *
     * @param append {@code true} to allow appending, {@code false} otherwise.
     */
    public static void setLogFileAppend(final boolean append) {
        setProperty(getLoggingConfigFile(), LOG_FILE_APPEND_PARAMETER, String.valueOf(append));
    }

    /**
     * Enable or disable the temporary files deletion.
     *
     * @param enable {@code true} to enable the deletion, {@code false} otherwise.
     */
    public static void enableTemporaryFilesDeletionOnExit(final boolean enable) {
        setProperty(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER, String.valueOf(enable));
    }

    /**
     * Get the temporary files max age parameter's value.
     *
     * @return The max age of temporary files in days.
     */
    public static Long getTemporaryFilesMaxAge() {
        final Long ageInSeconds = getLongProperty(TEMPORARY_FILES_MAX_AGE_PARAMETER);
        return ageInSeconds == null ? null : TimeUnit.SECONDS.toDays(ageInSeconds);
    }

    /**
     * Set the max age of temporary files before they are deleted.
     *
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
     * Get the default max recent presentations that must be stored and displayed in the "Open recent" menu.
     *
     * @return The default max recent presentations.
     */
    public static Long getDefaultMaxRecentPresentations() {
        return DEFAULT_MAX_RECENT_PRESENTATIONS;
    }

    /**
     * Get the maximum number of recent presentations that must be stored and displayed in the "Open recent" menu.
     *
     * @return The maximum number of recent presentations.
     */
    public static Long getMaxRecentPresentations() {
        final Long maxRecentPresentations = getLongProperty(MAX_RECENT_PRESENTATIONS);
        return maxRecentPresentations == null ? getDefaultMaxRecentPresentations() : maxRecentPresentations;
    }

    /**
     * Set the maximum number of recent presentations that must be stored and displayed in the "Open recent" menu.
     *
     * @param maxRecentPresentations The maximum number of recent presentations.
     */
    public static void setMaxRecentPresentations(final long maxRecentPresentations) {
        setProperty(MAX_RECENT_PRESENTATIONS, String.valueOf(maxRecentPresentations));
    }

    /**
     * Remove the maximum number of recent presentations that must be stored and displayed in the "Open recent" menu.
     */
    public static void removeMaxRecentPresentations() {
        removeProperty(MAX_RECENT_PRESENTATIONS);
    }

    /**
     * Get the default delay before taking snapshots of a browser.
     *
     * @return The default delay before taking snapshots of a browser.
     */
    public static Long getDefaultSnapshotDelay() {
        return DEFAULT_SNAPSHOT_DELAY;
    }

    /**
     * Get the delay before taking a snapshot of a browser.
     *
     * @return The delay before taking a snapshot of a browser.
     */
    public static Long getSnapshotDelay() {
        final Long snapshotDelay = getLongProperty(SNAPSHOT_DELAY);
        return snapshotDelay == null ? getDefaultSnapshotDelay() : snapshotDelay;
    }

    /**
     * Set the delay before taking a snapshot of a browser.
     *
     * @param snapshotDelay The delay before taking a snapshot of a browser.
     */
    public static void setSnapshotDelay(final long snapshotDelay) {
        setProperty(SNAPSHOT_DELAY, String.valueOf(snapshotDelay));
    }

    /**
     * Get the Twitter consumer key.
     *
     * @return The Twitter consumer key stored in the configuration.
     */
    public static String getTwitterConsumerKey() {
        return getProperty(TWITTER_CONSUMER_KEY);
    }

    /**
     * Set the Twitter consumer key.
     *
     * @param twitterConsumerKey The Twitter consumer key to store.
     */
    public static void setTwitterConsumerKey(final String twitterConsumerKey) {
        setProperty(TWITTER_CONSUMER_KEY, twitterConsumerKey);
    }

    /**
     * Get the Twitter consumer secret.
     *
     * @return The Twitter consumer secret stored in the configuration.
     */
    public static String getTwitterConsumerSecret() {
        return getProperty(TWITTER_CONSUMER_SECRET);
    }

    /**
     * Set the Twitter consumer secret.
     *
     * @param twitterConsumerSecret The Twitter consumer secret to store.
     */
    public static void setTwitterConsumerSecret(final String twitterConsumerSecret) {
        setProperty(TWITTER_CONSUMER_SECRET, twitterConsumerSecret);
    }

    /**
     * Get the default name of the theme to be used by the application.
     *
     * @return The default name of the theme to be used by the application.
     */
    public static String getDefaultThemeName() {
        return DEFAULT_THEME_NAME;
    }

    /**
     * Get the name of the theme defined for the application.
     *
     * @return The theme defined for the application.
     */
    public static String getThemeName() {
        final String theme = getProperty(THEME);
        return theme == null ? getDefaultThemeName() : theme;
    }

    /**
     * Set the name of the theme to be used by the application.
     *
     * @param theme The name of theme the application should use.
     */
    public static void setThemeName(final String theme) {
        String oldTheme = getThemeName();

        if (!oldTheme.equals(theme)) {
            setProperty(THEME, theme);
            OBSERVABLE.notifyThemeChanged(oldTheme, theme);
        }
    }

    /**
     * Get the default {@link Charset} used by the application.
     *
     * @return The default charset used by the application.
     */
    public static Charset getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    /**
     * Get a collection of presentations opened recently.
     *
     * @return The collection of presentations opened recently.
     */
    public static synchronized Set<RecentPresentation> getRecentPresentations() {
        if (RECENT_PRESENTATIONS == null) {
            try {
                RECENT_PRESENTATIONS = ContextFileWorker.readRecentPresentationFromFile(new File(getApplicationDirectory(), SLIDESHOWFX_CONTEXT_FILE_NAME));
            } catch (ContextFileException e) {
                LOGGER.log(WARNING, "Can not read the recent opened presentations", e);
            }
        }
        final Long maxRecentPresentations = getMaxRecentPresentations();

        synchronized (RECENT_PRESENTATIONS_LOCK) {
            if (RECENT_PRESENTATIONS.size() > maxRecentPresentations) {
                final File contextFile = new File(getApplicationDirectory(), SLIDESHOWFX_CONTEXT_FILE_NAME);
                try {
                    RECENT_PRESENTATIONS = ContextFileWorker.purgeRecentPresentations(contextFile, maxRecentPresentations);
                } catch (ContextFileException e) {
                    LOGGER.log(WARNING, "Can not purge recent presentations", e);
                }
            }
        }

        return RECENT_PRESENTATIONS;
    }

    /**
     * Save a {@link RecentPresentation} as a recently opened presentation. This save is persisted on disk.
     *
     * @param recentPresentation The presentation to save as recently opened.
     */
    public static synchronized void saveRecentPresentation(final RecentPresentation recentPresentation) {
        if (recentPresentation != null) {
            final File contextFile = new File(getApplicationDirectory(), SLIDESHOWFX_CONTEXT_FILE_NAME);
            boolean presentationAlreadyPresent = false;

            try {
                presentationAlreadyPresent = ContextFileWorker.recentPresentationAlreadyPresent(contextFile, recentPresentation);
            } catch (ContextFileException e) {
                LOGGER.log(WARNING, "Context file seems to not exist", e);
            }

            synchronized (RECENT_PRESENTATIONS) {
                if (RECENT_PRESENTATIONS.contains(recentPresentation)) {
                    RECENT_PRESENTATIONS.remove(recentPresentation);
                }
            }

            if (presentationAlreadyPresent) {
                try {
                    synchronized (RECENT_PRESENTATIONS) {
                        ContextFileWorker.updateRecentPresentationInFile(contextFile, recentPresentation);
                        RECENT_PRESENTATIONS.add(recentPresentation);
                    }
                } catch (ContextFileException e) {
                    LOGGER.log(WARNING, "Can not update recently opened presentation", e);
                }
            } else {
                try {
                    synchronized (RECENT_PRESENTATIONS) {
                        ContextFileWorker.saveRecentPresentationToFile(contextFile, recentPresentation);
                        RECENT_PRESENTATIONS.add(recentPresentation);
                    }
                } catch (ContextFileException e) {
                    LOGGER.log(WARNING, "The recent presentation couldn't be saved", e);
                }
            }

            synchronized (RECENT_PRESENTATIONS_LOCK) {
                final Long maxRecentPresentations = getMaxRecentPresentations();
                if (RECENT_PRESENTATIONS.size() > maxRecentPresentations) {
                    try {
                        RECENT_PRESENTATIONS = ContextFileWorker.purgeRecentPresentations(contextFile, maxRecentPresentations);
                    } catch (ContextFileException e) {
                        LOGGER.log(WARNING, "Can not purge recent presentations", e);
                    }
                }
            }
        }
    }
}
