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

import javafx.scene.Node;

/**
 * Defines the base interface that makes a plugin configurable.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public interface IConfigurable<T extends IPluginOptions> {

    /**
     * Get the base name for each configuration option. It can be used in order to store all configuration option within
     * the same base name in a file or in a database.
     * @return The base name for each configuration option of the plugin implementing the {@link IConfigurable}
     * interface.
     */
    String getConfigurationBaseName();

    /**
     * Get the user interface allowing to modify the options of a plugin.
     * @return The {@link Node} containing all graphical elements for modifying the options of a plugin.
     */
    Node getConfigurationUI();

    /**
     * Get the new options that can be saved.
     * @return The new options of the plugin's implementation.
     */
    T getNewOptions();

    /**
     * Saves the plugin's options. Usually saving the options means save them physically in a file, a database, etc.
     * The method should call {@link #getNewOptions()} in order to save new options.
     */
    void saveNewOptions();
}
