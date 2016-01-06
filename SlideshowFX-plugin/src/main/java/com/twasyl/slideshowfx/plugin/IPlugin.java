/*
 * Copyright 2016 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.plugin;

/**
 * Defines the base interface to create a plugin for SlideshowFX. A plugin is a piece of software
 * that can be used by SlideshowFX in order to add features to it.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
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
