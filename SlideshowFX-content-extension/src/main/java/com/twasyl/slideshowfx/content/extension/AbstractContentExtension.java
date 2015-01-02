/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.content.extension;

import java.io.*;
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
    protected final InputStream icon;
    protected final String toolTip;
    protected final String title;
    protected final String resourcesLocationPrefix;
    protected Set<Resource> resources = new LinkedHashSet<>();
    protected Set<String> resourcesLocations = new LinkedHashSet<>();

    /**
     * Creates a new instance of the content extension.
     * @param code The code of the content extension. Can not be null or empty.
     * @param icon The icon for this content extension that will be used in the SlideshowFX's UI.
     * @param toolTip The tooltip for this content extension that will be used in the SlideshowFX's UI.
     * @throws java.lang.NullPointerException If the code is null.
     * @throws java.lang.IllegalArgumentException If the code is empty.
     */
    protected AbstractContentExtension(String code, String resourcesLocationPrefix, InputStream icon, String toolTip, String title) {
        if(code == null) throw new NullPointerException("The code of the content extension is null");
        if(code.trim().isEmpty()) throw new IllegalArgumentException("The code of the content extension can not be empty");

        this.code = code.trim();
        this.resourcesLocationPrefix = resourcesLocationPrefix;
        this.icon = icon;
        this.toolTip = toolTip;
        this.title = title;
    }

    @Override
    public String getCode() { return this.code; }

    @Override
    public String getResourcesLocationPrefix() { return this.resourcesLocationPrefix; }

    @Override
    public Set<String> getResourcesLocation() { return this.resourcesLocations; }

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

    /**
     * This method allows to declare resources for this content extension and return this content extension.
     * @param location The location that will be added to the presentation of the resource.
     * @return This content extension.
     */
    protected AbstractContentExtension putResourceLocation(String location) {
        if(location != null && !location.isEmpty()) {
            this.resourcesLocations.add(location);
        }

        return this;
    }

    @Override
    public void extractResources(File directory) {
        if(directory == null) throw new NullPointerException("The directory where to extract the resources can not be null");
        if(!directory.exists()) throw new IllegalArgumentException("The directory where to extract the resources doesn't exist");

        if(!this.resourcesLocations.isEmpty()) {

            File destinationFile;
            byte[] buffer = new byte[1024];
            int bytesRead;

            for(String resource : this.resourcesLocations) {
                destinationFile = new File(directory, resource);

                if(!destinationFile.getParentFile().exists()) {
                    destinationFile.getParentFile().mkdirs();
                }

                try (final FileOutputStream out = new FileOutputStream(destinationFile);
                     final InputStream in = getClass().getResourceAsStream(getResourcesLocationPrefix().concat(resource))) {

                    while (in != null && (bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    out.flush();
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Can not extract resource", e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Can not extract resource", e);
                }
            }
        }
    }

    @Override
    public InputStream getIcon() { return this.icon; }

    @Override
    public String getToolTip() { return this.toolTip; }

    @Override
    public String getTitle() { return this.title; }
}
