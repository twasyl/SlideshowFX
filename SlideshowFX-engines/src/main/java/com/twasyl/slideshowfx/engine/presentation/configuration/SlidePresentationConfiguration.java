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

package com.twasyl.slideshowfx.engine.presentation.configuration;

import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplateConfiguration;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a slide of the presentation
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SlidePresentationConfiguration {
    private SlideTemplateConfiguration template;
    private String id;
    private String slideNumber;
    private Image thumbnail;
    private final Map<String, SlideElementConfiguration> elements = new HashMap<>();

    public SlidePresentationConfiguration() {
    }

    public SlidePresentationConfiguration(String slideNumber) {
        this.slideNumber = slideNumber;
    }

    public SlidePresentationConfiguration(SlideTemplateConfiguration template, String slideNumber) {
        this.template = template;
        this.slideNumber = slideNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public SlideTemplateConfiguration getTemplate() { return template; }
    public void setTemplate(SlideTemplateConfiguration template) { this.template = template; }

    public String getSlideNumber() { return slideNumber; }
    public void setSlideNumber(String slideNumber) { this.slideNumber = slideNumber; }

    public Image getThumbnail() { return thumbnail; }
    public void setThumbnail(Image thumbnail) { this.thumbnail = thumbnail; }

    /**
     * The elements contained in the slide. The key represents the ID of each element in the slide.
     * The given content should not be given in Base64.
     * @return The map containing the slide elements.
     */
    public Map<String, SlideElementConfiguration> getElements() { return elements; }

    public void updateElement(String elementId, String code, String originalContent, String htmlContent) {
        Optional<SlideElementConfiguration> slideElement = getElements().entrySet()
                .stream().filter(entry -> entry.getKey().equals(elementId))
                .map(entry -> entry.getValue())
                .findFirst();

        if(slideElement.isPresent()) {
            slideElement.get().setOriginalContentCode(code);
            slideElement.get().setOriginalContent(originalContent);
            slideElement.get().setHtmlContent(htmlContent);
        } else {
            SlideElementConfiguration se = new SlideElementConfiguration();
            se.setId(elementId);
            se.setOriginalContentCode(code);
            se.setOriginalContent(originalContent);
            se.setHtmlContent(htmlContent);

            getElements().put(elementId, se);
        }
    }
}
