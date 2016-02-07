package com.twasyl.slideshowfx.utils.time;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set of utility methods for the Jav Date/Time API.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class DateTimeUtils {
    private static final Logger LOGGER = Logger.getLogger(DateTimeUtils.class.getName());

    /**
     * Convert a given {@link LocalDateTime} to an {@link Instant}. In order to convert it, the method
     * {@link LocalDateTime#toInstant(ZoneOffset)} is called with the offset determined using
     * {@link ZoneOffset#of(String)} with {@link ZoneOffset#UTC} as argument.
     * @param dateTime The date/time to convert to an {@link Instant}.
     * @return The {@link Instant} corresponding to the provided date/time.
     */
    public static Instant toInstant(final LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.of(ZoneOffset.UTC.toString()));
    }

    /**
     * Compares the {@link FileTime} of two {@link File files} and return the result. This method calls the
     * {@link java.nio.file.attribute.FileTime#compareTo(Object)} method with {@code file2} as argument of the call.
     * @param file1 The file which {@link FileTime} will be used as caller of {@link FileTime#compareTo(Object)}.
     * @param file2 The file which is used as callee of {@link FileTime#compareTo(Object)}.
     * @return The result of the {@link java.nio.file.attribute.FileTime#compareTo(Object)} method.
     */
    public static int sortByFileTime(final File file1, final File file2) {
        int comparison = 0;

        final FileTime checkedTime;
        final FileTime referenceTime;

        try {
            checkedTime = Files.getLastModifiedTime(file1.toPath());
            referenceTime = Files.getLastModifiedTime(file2.toPath());
            comparison = checkedTime.compareTo(referenceTime);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not check if a file is older than another", e);
        }

        return comparison;
    }

    /**
     * Get a {@link Predicate<File>} filtering files than are older than a given number of days.
     * @param days The number of days for files to be filtered.
     * @return The {@link Predicate<File>} filtering files older than a given number of days.
     */
    public static Predicate<File> getFilterForFilesOlderThanGivenDays(final long days) {
        final Instant maxAge = toInstant(LocalDateTime.now().minusDays(days));

        return file -> {
            boolean canDelete = false;
            try {
                final Instant lastModifiedTime = Files.getLastModifiedTime(file.toPath()).toInstant();
                canDelete = lastModifiedTime.isBefore(maxAge);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not determine last modification time", e);
            }

            return canDelete;
        };
    }
}
