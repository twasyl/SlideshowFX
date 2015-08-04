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

package com.twasyl.slideshowfx.content.extension;

import com.twasyl.slideshowfx.utils.ZipUtils;
import de.jensd.fx.glyphs.GlyphIcons;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines the basic behavior of a content extension.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public abstract class AbstractContentExtension implements IContentExtension {
    private static final Logger LOGGER = Logger.getLogger(AbstractContentExtension.class.getName());

    protected final String code;
    protected final GlyphIcons icon;
    protected final String toolTip;
    protected final String title;
    protected final URL resourcesArchive;
    protected Set<Resource> resources = new LinkedHashSet<>();

    /**
     * Creates a new instance of the content extension.
     * @param code The code of the content extension. Can not be null or empty.
     * @param resourcesArchive The archive that contains all resources that will be extracted for the presentation.
     * @param icon The icon for this content extension that will be used in the SlideshowFX's UI.
     * @param toolTip The tooltip for this content extension that will be used in the SlideshowFX's UI.
     * @param title The title of the window of this content extension.
     * @throws java.lang.NullPointerException If the code is null.
     * @throws java.lang.IllegalArgumentException If the code is empty.
     */
    protected AbstractContentExtension(String code, URL resourcesArchive, GlyphIcons icon, String toolTip, String title) {
        if(code == null) throw new NullPointerException("The code of the content extension is null");
        if(code.trim().isEmpty()) throw new IllegalArgumentException("The code of the content extension can not be empty");

        this.code = code.trim();
        this.resourcesArchive = resourcesArchive;
        this.icon = icon;
        this.toolTip = toolTip;
        this.title = title;
    }

    @Override
    public String getCode() { return this.code; }

    @Override
    public URL getResourcesArchive() { return this.resourcesArchive; }

    @Override
    public Set<Resource> getResources() { return this.resources; }

    /**
     * This method allows to declare resources for this content extension and return this content extension.
     * @param type The type of the resource
     * @param content The content that will be added to the presentation of the resource.
     * @return This content extension.
     */
    protected AbstractContentExtension putResource(ResourceType type, String content) {
        if(content != null && !content.isEmpty()) {
            this.resources.add(new Resource(type, content));
        }

        return this;
    }

    @Override
    public void extractResources(File directory) {
        if(directory == null) throw new NullPointerException("The directory where to extract the resources can not be null");
        if(!directory.exists()) throw new IllegalArgumentException("The directory where to extract the resources doesn't exist");

        try {
            ZipUtils.unzip(this.getClass().getResourceAsStream(this.getResourcesArchive().getFile()), directory);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not extract the resources", e);
        }
    }

    @Override
    public GlyphIcons getIcon() { return this.icon; }

    @Override
    public String getToolTip() { return this.toolTip; }

    @Override
    public String getTitle() { return this.title; }
}
