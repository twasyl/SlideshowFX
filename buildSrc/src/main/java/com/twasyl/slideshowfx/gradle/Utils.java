package com.twasyl.slideshowfx.gradle;

import org.gradle.api.Project;

/**
 * Utility class providing tools to be used for the gradle build.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Utils {

    private Utils() {
    }

    /**
     * Get the project version without the {@code -SNAPSHOT} qualifier. If the version doesn't contain {@code -SNAPSHOT}
     * then the version, as is, is returned.
     *
     * @param project The project for which the version should be stripped.
     * @return The stripped project version.
     */
    public static String stripProjectVersion(final Project project) {
        final String version = project.getVersion().toString();
        if (version.contains("-SNAPSHOT")) {
            return version.replace("-SNAPSHOT", "");
        } else {
            return version;
        }
    }
}
