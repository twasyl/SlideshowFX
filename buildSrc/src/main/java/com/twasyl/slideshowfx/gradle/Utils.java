package com.twasyl.slideshowfx.gradle;

import org.gradle.api.Project;

import java.io.File;

/**
 * Utility class providing tools to be used for the gradle build.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Utils {

    private static String OS = System.getProperty("os.name").toLowerCase();
    public static String BUILD_JDK_PROPERTY = "build_jdk";

    private Utils() {
    }

    public static boolean isWindows() {
        return OS.indexOf("win") >= 0;
    }

    public static boolean isMac() {
        return OS.indexOf("mac") >= 0;
    }

    public static boolean isLinux() {
        return OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0 || OS.indexOf("aix") >= 0;
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

    /**
     * Checks if the given project has the property {@value BUILD_JDK_PROPERTY} defined.
     *
     * @param project The project to check.
     * @return {@code true} if the property is defined, {@code false} otherwise.
     */
    public static boolean hasBuildJdkProperty(final Project project) {
        return project.hasProperty(BUILD_JDK_PROPERTY);
    }

    /**
     * Get the JDK home of the property {@value BUILD_JDK_PROPERTY} for the given project.
     *
     * @param project The project to use.
     * @return the value of the {@value BUILD_JDK_PROPERTY}.
     * @see #hasBuildJdkProperty(Project)
     */
    public static String getBuildJdkHome(final Project project) {
        return project.property(BUILD_JDK_PROPERTY).toString();
    }

    /**
     * Get a desired binary present in the JDK installation.
     *
     * @param project The project used to get the binary.
     * @param binary  The name of the binary to get, e.g. {@code java}.
     * @return The full path (including the binary name) of the desired binary.
     */
    public static String getJavaBinary(final Project project, String binary) {
        if (hasBuildJdkProperty(project)) {
            return getBuildJdkHome(project) + File.separator + "bin" + File.separator + binary;
        } else {
            return binary;
        }
    }
}
