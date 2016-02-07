package com.twasyl.slideshowfx.utils.time;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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

    @BeforeClass
    public static void setUp() throws IOException {
        final TimeUnit daysUnit = TimeUnit.DAYS;
        final Instant now = Instant.now();

        file_10_daysOld.mkdir();
        Files.setLastModifiedTime(file_10_daysOld.toPath(), FileTime.from(now.minusSeconds(daysUnit.toSeconds(10))));

        file_15_daysOld.mkdir();
        Files.setLastModifiedTime(file_15_daysOld.toPath(), FileTime.from(now.minusSeconds(daysUnit.toSeconds(30))));

        file_10_daysMoreRecent.mkdir();
        Files.setLastModifiedTime(file_10_daysMoreRecent.toPath(), FileTime.from(now.plusSeconds(daysUnit.toSeconds(10))));

        file_15_daysMoreRecent.mkdir();
        Files.setLastModifiedTime(file_15_daysMoreRecent.toPath(), FileTime.from(now.plusSeconds(daysUnit.toSeconds(15))));
    }

    @AfterClass
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
        assertEquals(2, filesToDelete.size());

        assertEquals(file_15_daysOld, filesToDelete.get(0));
        assertEquals(file_10_daysOld, filesToDelete.get(1));
    }
}
