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

package com.twasyl.slideshowfx.engine.template.configuration;

import com.twasyl.slideshowfx.engine.template.DynamicAttribute;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a slide defined by the template.
 *
 * @author Thierry Wasylczenko
 */
public class SlideTemplate {
    private int id;
    private String name;
    private File file;
    // private String[] dynamicIds;
    private DynamicAttribute[] dynamicAttributes;
    private SlideElementTemplate[] elements;

    public SlideTemplate() {
    }

    public SlideTemplate(int id, String name, File file) {
        this.id = id;
        this.name = name;
        this.file = file;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }

    /* public String[] getDynamicIds() { return dynamicIds; }
    public void setDynamicIds(String[] dynamicIds) { this.dynamicIds = dynamicIds; } */

    public DynamicAttribute[] getDynamicAttributes() { return dynamicAttributes; }
    public void setDynamicAttributes(DynamicAttribute[] dynamicAttributes) { this.dynamicAttributes = dynamicAttributes; }

    public SlideElementTemplate[] getElements() { return elements; }
    public void setElements(SlideElementTemplate[] elements) { this.elements = elements; }

    /**
     * Search for a {@link SlideElementTemplate} corresponding to the given {@code id}. If the element is not found
     * {@code null} is returned.
     *
     * @param id The ID of the SlideElementTemplate to find.
     * @return The SlideElementTemplate corresponding to the given ID, or {@code null} if it is not found.
     */
    public SlideElementTemplate getSlideElementTemplate(int id) {
        Optional<SlideElementTemplate> result = Arrays.stream(this.elements).
                filter(element -> element.getId() == id)
                .findFirst();

        return result.isPresent() ? result.get() : null;
    }
}
