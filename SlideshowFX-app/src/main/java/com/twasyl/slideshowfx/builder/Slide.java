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

package com.twasyl.slideshowfx.builder;

import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a slide of the presentation
 */
public class Slide {
    private SlideTemplate template;
    private String id;
    private String slideNumber;
    private Image thumbnail;
    private final Map<String, SlideElement> elements = new HashMap<>();

    public Slide() {
    }

    public Slide(String slideNumber) {
        this.slideNumber = slideNumber;
    }

    public Slide(SlideTemplate template, String slideNumber) {
        this.template = template;
        this.slideNumber = slideNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public SlideTemplate getTemplate() { return template; }
    public void setTemplate(SlideTemplate template) { this.template = template; }

    public String getSlideNumber() { return slideNumber; }
    public void setSlideNumber(String slideNumber) { this.slideNumber = slideNumber; }

    public Image getThumbnail() { return thumbnail; }
    public void setThumbnail(Image thumbnail) { this.thumbnail = thumbnail; }

    /**
     * The elements contained in the slide. The key represents the ID of each element in the slide.
     * The given content should not be given in Base64.
     * @return
     */
    public Map<String, SlideElement> getElements() { return elements; }

    public void updateElement(String elementId, String code, String originalContent, String htmlContent) {
        Optional<SlideElement> slideElement = getElements().entrySet()
                .stream().filter(entry -> entry.getKey().equals(elementId))
                .map(entry -> entry.getValue())
                .findFirst();

        if(slideElement.isPresent()) {
            slideElement.get().setOriginalContentCode(code);
            slideElement.get().setOriginalContent(originalContent);
            slideElement.get().setHtmlContent(htmlContent);
        } else {
            SlideElement se = new SlideElement();
            se.setId(elementId);
            se.setOriginalContentCode(code);
            se.setOriginalContent(originalContent);
            se.setHtmlContent(htmlContent);

            getElements().put(elementId, se);
        }
    }
}
