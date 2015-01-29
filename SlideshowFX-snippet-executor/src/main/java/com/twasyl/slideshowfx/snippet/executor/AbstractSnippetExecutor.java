/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.snippet.executor;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of a {@link com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor}. It takes care of
 * defining the {@link #getCode()}, {@link #getLanguage()}, {@link #getCssClass()} and {@link #getSdkHome()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractSnippetExecutor implements ISnippetExecutor {
    private static final Logger LOGGER = Logger.getLogger(AbstractSnippetExecutor.class.getName());

    /*
     * Constants for stored properties
     */
    private static final String PROPERTIES_PREFIX = "snippet.executor.";
    protected final String HOME;

    private final String code;
    private final String language;
    private final String cssClass;
    private File sdkHome;

    protected AbstractSnippetExecutor(final String code, final String language, final String cssClass) {
        this.code = code;
        this.language = language;
        this.cssClass = cssClass;

        this.HOME = PROPERTIES_PREFIX.concat(this.code).concat(".home");

        String property = GlobalConfiguration.getProperty(this.HOME);
        if(property != null && !property.isEmpty()) {
            try {
                this.setSdkHome(new File(property));
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "Can not load the SDK home property", e);
            }
        }
    }

    protected File getTemporaryDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    @Override
    public String getCode() { return this.code; }

    @Override
    public String getLanguage() { return this.language; }

    @Override
    public String getCssClass() { return this.cssClass; }

    @Override
    public File getSdkHome() { return this.sdkHome; }

    @Override
    public void setSdkHome(File sdkHome) throws FileNotFoundException {
        if(sdkHome == null) throw new NullPointerException("The sdkHome can not be null");
        if(!sdkHome.exists()) throw new FileNotFoundException("The sdkHome doesn't exist");
        if(!sdkHome.isDirectory()) throw new IllegalArgumentException("The sdkHome is not a directory");

        this.sdkHome = sdkHome;
    }

    @Override
    public void saveSdkHome(File sdkHome) throws FileNotFoundException {
        this.setSdkHome(sdkHome);
        GlobalConfiguration.setProperty(this.HOME, sdkHome.getAbsolutePath().replaceAll("\\\\", "/"));
    }
}