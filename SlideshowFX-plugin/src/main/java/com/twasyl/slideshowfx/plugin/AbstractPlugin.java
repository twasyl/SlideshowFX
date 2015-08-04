/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.plugin;

/**
 * A basic implementation of a {@link IPlugin}.
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class AbstractPlugin<T extends IPluginOptions> implements IPlugin<T> {
    private String name;
    private T options;

    /**
     * The constructor to create an instance of a {@link IPlugin}.
     * @param name The name of the plugin.
     */
    protected AbstractPlugin(final String name) {
        this.name = name;
        this.options = null;
    }

    /**
     * Creates a {@link IPlugin} with a given name and default options.
     * @param name The name of the plugin.
     * @param options The options of the plugin.
     */
    protected AbstractPlugin(final String name, final T options) {
        this(name);
        this.options = options;
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public T getOptions() { return this.options; }

    @Override
    public void setOptions(T options) throws NullPointerException {
        if(options == null) throw new NullPointerException("The options can not be null");

        this.options = options;
    }
}
