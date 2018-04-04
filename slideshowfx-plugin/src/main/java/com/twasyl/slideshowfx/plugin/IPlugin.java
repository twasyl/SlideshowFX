package com.twasyl.slideshowfx.plugin;

/**
 * Defines the base interface to create a plugin for SlideshowFX. A plugin is a piece of software
 * that can be used by SlideshowFX in order to add features to it.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public interface IPlugin<T extends IPluginOptions> {

    /**
     * Get the name of the plugin.
     * @return The name of the plugin.
     */
    String getName();

    /**
     * Get the options of the plugins. Options defines the custom parameters of a plugin and each plugin can defines it's
     * own options.
     * @return The options of the plugin.
     */
    T getOptions();

    /**
     * Saves the new options of a plugin.
     * @param options New options of a plugin.
     * @throws NullPointerException If the specified options are {@code null}.
     */
    void setOptions(final T options) throws NullPointerException;
}
