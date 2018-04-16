package com.twasyl.slideshowfx.utils;

/**
 * This class allows to determine which OS is currently in use.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 2.0
 */
public class OSUtils {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.indexOf("win") >= 0;
    }

    public static boolean isMac() {
        return OS.indexOf("mac") >= 0;
    }

    public static boolean isLinux() {
        return OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0 || OS.indexOf("ais") >= 0;
    }
}
