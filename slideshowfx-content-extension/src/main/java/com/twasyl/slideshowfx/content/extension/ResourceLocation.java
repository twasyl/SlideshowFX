package com.twasyl.slideshowfx.content.extension;

/**
 * Enumeration representing the location of {@link Resource resources} used by a plugin.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public enum ResourceLocation {
    /**
     * Indicates the {@link Resource} is shipped with the plugin itself.
     */
    INTERNAL,
    /**
     * Indicates the {@link Resource} is shipped outside the plugin itself. It may typically be provided the
     * SlideshowFX itself.
     */
    EXTERNAL;
}
