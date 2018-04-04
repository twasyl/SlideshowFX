package com.twasyl.slideshowfx.utils.time;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX
 */
public class DateTimeUtilsTest {

    private static final File TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
    private static final String TEMP_FILE_PREFIX = "test-sfx-";

    private static final File file_10_daysOld= new File(TEMP_DIRECTORY, TEMP_FILE_PREFIX + "01");
    private static final File file_15_daysOld = new File(TEMP_DIRECTORY, TEMP_FILE_PREFIX + "02");
    private static final File file_10_daysMoreRecent = new File(TEMP_DIRECTORY, TEMP_FILE_PREFIX + "03");
    private static final File file_15_daysMoreRecent = new File(TEMP_DIRECTORY, TEMP_FILE_PREFIX + "04");

    @BeforeAll
    public static void setUp() throws IOException {
        final Instant now = Instant.now(Clock.systemUTC());

        file_10_daysOld.deleteOnExit();
        file_10_daysOld.mkdir();
        Files.setLastModifiedTime(file_10_daysOld.toPath(), FileTime.from(now.minus(10, DAYS)));

        file_15_daysOld.deleteOnExit();
        file_15_daysOld.mkdir();
        Files.setLastModifiedTime(file_15_daysOld.toPath(), FileTime.from(now.minus(15, DAYS)));

        file_10_daysMoreRecent.deleteOnExit();
        file_10_daysMoreRecent.mkdir();
        Files.setLastModifiedTime(file_10_daysMoreRecent.toPath(), FileTime.from(now.plus(10, DAYS)));

        file_15_daysMoreRecent.deleteOnExit();
        file_15_daysMoreRecent.mkdir();
        Files.setLastModifiedTime(file_15_daysMoreRecent.toPath(), FileTime.from(now.plus(15, DAYS)));
    }

    @AfterAll
    public static void tearDown() {
        file_10_daysOld.delete();
        file_15_daysOld.delete();
        file_10_daysMoreRecent.delete();
        file_15_daysMoreRecent.delete();
    }

    @Test
    public void testFilesToDeleteOlderThan15Days() {

        final List<File> filesToDelete = Arrays.stream(TEMP_DIRECTORY.listFiles())
                .filter(file -> file.getName().startsWith(TEMP_FILE_PREFIX))
                .filter(DateTimeUtils.getFilterForFilesOlderThanGivenDays(15))
                .collect(Collectors.toList());

        assertNotNull(filesToDelete);
        assertFalse(filesToDelete.isEmpty());
        assertEquals(1, filesToDelete.size());

        assertEquals(file_15_daysOld, filesToDelete.get(0));
    }

    @Test
    public void testFilesToDeleteOlderThan10Days() throws IOException {

        final List<File> filesToDelete = Arrays.stream(TEMP_DIRECTORY.listFiles())
                .filter(file -> file.getName().startsWith(TEMP_FILE_PREFIX))
                .filter(DateTimeUtils.getFilterForFilesOlderThanGivenDays(10))
                .sorted(DateTimeUtils::sortByFileTime)
                .collect(Collectors.toList());

        assertNotNull(filesToDelete);
        assertFalse(filesToDelete.isEmpty());
        assertEquals(2, filesToDelete.size(), filesToDelete.toString());

        assertEquals(file_15_daysOld, filesToDelete.get(0));
        assertEquals(file_10_daysOld, filesToDelete.get(1));
    }
}
