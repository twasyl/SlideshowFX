package com.twasyl.slideshowfx.global.configuration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.logging.Level;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class GlobalConfigurationDefinitionTest {

    private static File tmpFolder;

    @BeforeAll
    public static void setUp() {
        tmpFolder = new File("build", "testsTmp");
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }

        System.setProperty(APPLICATION_DIRECTORY_PROPERTY, tmpFolder.getAbsolutePath());

        GlobalConfiguration.createConfigurationFile();
        GlobalConfiguration.createLoggingConfigurationFile();

        GlobalConfiguration.fillConfigurationWithDefaultValue();
        GlobalConfiguration.fillLoggingConfigurationFileWithDefaultValue();
    }

    @AfterAll
    public static void tearDown() {
        GlobalConfiguration.getConfigurationFile().delete();
        GlobalConfiguration.getLoggingConfigFile().delete();
        GlobalConfiguration.getApplicationDirectory().delete();
    }

    @Test
    public void autoSavingEnabled() {
        GlobalConfiguration.enableAutoSaving(true);
        assertTrue(GlobalConfiguration.isAutoSavingEnabled());
    }

    @Test
    public void autoSavingDisabled() {
        GlobalConfiguration.enableAutoSaving(false);
        assertFalse(GlobalConfiguration.isAutoSavingEnabled());
    }

    @Test
    public void autoSavingInterval100() {
        GlobalConfiguration.setAutoSavingInterval(100);
        assertEquals(100l, (long) GlobalConfiguration.getAutoSavingInterval());
    }

    @Test
    public void autoSavingInterval500() {
        GlobalConfiguration.setAutoSavingInterval(500);
        assertEquals(500l, (long) GlobalConfiguration.getAutoSavingInterval());
    }

    @Test
    public void temporaryFilesDeletionOnExitEnabled() {
        GlobalConfiguration.enableTemporaryFilesDeletionOnExit(true);
        assertTrue(GlobalConfiguration.isTemporaryFilesDeletionOnExitEnabled());
    }

    @Test
    public void temporaryFilesDeletionOnExitDisabled() {
        GlobalConfiguration.enableTemporaryFilesDeletionOnExit(false);
        assertFalse(GlobalConfiguration.isTemporaryFilesDeletionOnExitEnabled());
    }

    @Test
    public void temporaryFilesMaxAge100() {
        GlobalConfiguration.setTemporaryFilesMaxAge(100);
        assertEquals(100l, (long) GlobalConfiguration.getTemporaryFilesMaxAge());
    }

    @Test
    public void temporaryFilesMaxAge500() {
        GlobalConfiguration.setTemporaryFilesMaxAge(500);
        assertEquals(500l, (long) GlobalConfiguration.getTemporaryFilesMaxAge());
    }

    @Test
    public void setInfoLogLevel() {
        GlobalConfiguration.setLogLevel(Level.INFO);

        final String property = GlobalConfiguration.getProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_LEVEL_PARAMETER);
        assertEquals(Level.INFO.getName(), property);
    }

    @Test
    public void setSevereLogLevel() {
        GlobalConfiguration.setLogLevel(Level.SEVERE);

        final String property = GlobalConfiguration.getProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_LEVEL_PARAMETER);
        assertEquals(Level.SEVERE.getName(), property);
    }

    @Test
    public void enableLogFileAppend() {
        GlobalConfiguration.setLogFileAppend(true);

        final Boolean property = GlobalConfiguration.getBooleanProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_FILE_APPEND_PARAMETER);
        assertTrue(property);
    }

    @Test
    public void disableLogFileAppend() {
        GlobalConfiguration.setLogFileAppend(false);

        final Boolean property = GlobalConfiguration.getBooleanProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_FILE_APPEND_PARAMETER);
        assertFalse(property);
    }

    @Test
    public void setLogFileLimit100() {
        GlobalConfiguration.setLogFileLimit(100);

        final Long property = GlobalConfiguration.getLongProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_FILE_LIMIT_PARAMETER);
        assertEquals(100l, (long) property);
    }

    @Test
    public void setLogFileLimit500() {
        GlobalConfiguration.setLogFileLimit(500);

        final Long property = GlobalConfiguration.getLongProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_FILE_LIMIT_PARAMETER);
        assertEquals(500l, (long) property);
    }

    @Test
    public void setLogFilePattern() {
        GlobalConfiguration.setLogFilePattern("abc");

        final String property = GlobalConfiguration.getProperty(GlobalConfiguration.getLoggingConfigFile(), LOG_FILE_PATTERN_PARAMETER);
        assertEquals("abc", property);
    }
}
