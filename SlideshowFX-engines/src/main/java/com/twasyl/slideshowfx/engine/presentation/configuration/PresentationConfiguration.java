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

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.IConfiguration;
import javafx.scene.image.Image;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a presentation
 */
public class PresentationConfiguration implements IConfiguration {
    private static final Logger LOGGER = Logger.getLogger(PresentationConfiguration.class.getName());
    public static final String DEFAULT_PRESENTATION_FILENAME = "presentation.html";

    private Document document;
    private File presentationFile;
    private Set<Resource> customResources = new LinkedHashSet<>();
    private List<SlidePresentationConfiguration> slides = new ArrayList<>();

    public File getPresentationFile() { return presentationFile; }
    public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

    public List<SlidePresentationConfiguration> getSlides() { return slides; }
    public void setSlides(List<SlidePresentationConfiguration> slides) { this.slides = slides; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public Set<Resource> getCustomResources() { return customResources; }

    public void updateSlideThumbnail(String slideNumber, Image image) {
        if(slideNumber == null) throw new IllegalArgumentException("The slide number can not be null");

        SlidePresentationConfiguration slideToUpdate = null;
        for (SlidePresentationConfiguration s : getSlides()) {
            if (slideNumber.equals(s.getSlideNumber())) {
                s.setThumbnail(image);
                LOGGER.finest("Slide's thumbnail updated");
                break;
            }
        }
    }

    /**
     * Get a slide by it's slide number.
     * @param slideNumber The slide number og the slide to get.
     * @return The slide or null if not found.
     */
    public SlidePresentationConfiguration getSlideByNumber(String slideNumber) {
        SlidePresentationConfiguration slide = null;

        Optional<SlidePresentationConfiguration> slideOpt = slides.stream()
                .filter(s -> slideNumber.equals(s.getSlideNumber()))
                .findFirst();

        if(slideOpt.isPresent()) slide = slideOpt.get();

        return slide;
    }

    /**
     * Get a slide by it's ID.
     * @param id The ID of the slide to get.
     * @return The slide or null if not found.
     */
    public SlidePresentationConfiguration getSlideById(String id) {
        SlidePresentationConfiguration slide = null;

        Optional<SlidePresentationConfiguration> slideOpt = slides.stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst();

        if(slideOpt.isPresent()) slide = slideOpt.get();

        return slide;
    }

    /**
     * Update the given {@code slide} in the HTML file. Each {@link com.twasyl.slideshowfx.engine.presentation.configuration.SlideElementConfiguration}
     * of the {@code slide} in the HTML document.
     * @param slide The slide to update in the HTML document.
     */
    public void updateSlideInDocument(SlidePresentationConfiguration slide) {
        if(slide == null) throw new IllegalArgumentException("The slide can not be null");

        slide.getElements().values()
                .stream()
                .forEach(element -> this.document.getElementById(element.getId())
                                                 .html(element.getHtmlContent()));
    }
}
