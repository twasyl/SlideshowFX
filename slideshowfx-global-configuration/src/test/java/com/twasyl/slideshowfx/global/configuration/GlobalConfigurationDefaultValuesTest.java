package com.twasyl.slideshowfx.global.configuration;

import com.twasyl.slideshowfx.logs.SlideshowFXHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class GlobalConfigurationDefaultValuesTest {

    private static File tmpFolder;
    private static Properties loggingConfigProperties;
    private static Properties applicationProperties;

    @BeforeAll
    public static void setUp() {
        tmpFolder = new File("build", "testsTmp");
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }

        applicationProperties = new Properties();
        loggingConfigProperties = new Properties();

        System.setProperty(APPLICATION_DIRECTORY_PROPERTY, tmpFolder.getAbsolutePath());

        GlobalConfiguration.createConfigurationFile();
        GlobalConfiguration.createLoggingConfigurationFile();

        GlobalConfiguration.fillConfigurationWithDefaultValue();
        GlobalConfiguration.fillLoggingConfigurationFileWithDefaultValue();

        try (final FileInputStream input = new FileInputStream(GlobalConfiguration.getConfigurationFile())) {
            applicationProperties.load(input);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try (final FileInputStream input = new FileInputStream(GlobalConfiguration.getLoggingConfigFile())) {
            loggingConfigProperties.load(input);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    public static void tearDown() {
        GlobalConfiguration.getConfigurationFile().delete();
        GlobalConfiguration.getLoggingConfigFile().delete();
        GlobalConfiguration.getApplicationDirectory().delete();
    }

    @Test
    public void hasLogLevelParameter() {
        assertTrue(loggingConfigProperties.containsKey(LOG_LEVEL_PARAMETER));
    }

    @Test
    public void hasLogHandlersParameter() {
        assertTrue(loggingConfigProperties.containsKey(LOG_HANDLERS_PARAMETER));
    }

    @Test
    public void hasLogEncodingSuffixForFileHandler() {
        assertTrue(loggingConfigProperties.containsKey(FileHandler.class.getName().concat(LOG_ENCODING_SUFFIX)));
    }

    @Test
    public void hasLogEncodingSuffixForSlideshowFXHandler() {
        assertTrue(loggingConfigProperties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_ENCODING_SUFFIX)));
    }

    @Test
    public void hasLogFileLimitParameter() {
        assertTrue(loggingConfigProperties.containsKey(LOG_FILE_LIMIT_PARAMETER));
    }

    @Test
    public void hasLogFilePatternParameter() {
        assertTrue(loggingConfigProperties.containsKey(LOG_FILE_PATTERN_PARAMETER));
    }

    @Test
    public void hasLogFormatterSuffixForFileHandler() {
        assertTrue(loggingConfigProperties.containsKey(FileHandler.class.getName().concat(LOG_FORMATTER_SUFFIX)));
    }

    @Test
    public void hasLogFormatterSuffixForSlideshowFXHandler() {
        assertTrue(loggingConfigProperties.containsKey(SlideshowFXHandler.class.getName().concat(LOG_FORMATTER_SUFFIX)));
    }

    @Test
    public void hasLogFileAppendParameter() {
        assertTrue(loggingConfigProperties.containsKey(LOG_FILE_APPEND_PARAMETER));
    }

    @Test
    public void hasTemporaryFilesDeletionOnExit() {
        assertTrue(applicationProperties.containsKey(TEMPORARY_FILES_DELETION_ON_EXIT_PARAMETER));
    }

    @Test
    public void hasTemporaryFilesMaxAge() {
        assertTrue(applicationProperties.containsKey(TEMPORARY_FILES_MAX_AGE_PARAMETER));
    }

    @Test
    public void hasAutoSavingEnabled() {
        assertTrue(applicationProperties.containsKey(AUTO_SAVING_ENABLED_PARAMETER));
    }

    @Test
    public void hasAutoSavingInterval() {
        assertTrue(applicationProperties.containsKey(AUTO_SAVING_INTERVAL_PARAMETER));
    }
}
