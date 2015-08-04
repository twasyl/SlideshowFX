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

package com.twasyl.slideshowfx.snippet.executor;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import com.twasyl.slideshowfx.plugin.IConfigurable;
import javafx.scene.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of a {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}. It takes care of
 * defining the {@link #getCode()}, {@link #getLanguage()}, {@link #getCssClass()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractSnippetExecutor<T extends ISnippetExecutorOptions> extends AbstractPlugin<T> implements ISnippetExecutor<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractSnippetExecutor.class.getName());

    /*
     * Constants for stored properties
     */
    private static final String PROPERTIES_PREFIX = "snippet.executor.";
    protected T newOptions;

    private final String configurationBaseName;
    private final String code;
    private final String language;
    private final String cssClass;

    protected AbstractSnippetExecutor(final String code, final String language, final String cssClass) {
        super(code);
        this.code = code;
        this.language = language;
        this.cssClass = cssClass;

        this.configurationBaseName = PROPERTIES_PREFIX.concat(this.code);
    }

    @Override
    public String getConfigurationBaseName() { return this.configurationBaseName; }

    @Override
    public T getNewOptions() { return this.newOptions; }

    protected File getTemporaryDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public String getCode() { return this.code; }

    @Override
    public String getLanguage() { return this.language; }

    @Override
    public String getCssClass() { return this.cssClass; }
}