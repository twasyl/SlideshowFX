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

package com.twasyl.slideshowfx.engine.presentation.configuration;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.IConfiguration;
import com.twasyl.slideshowfx.utils.beans.Pair;
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

    private long id;
    private Document document;
    private File presentationFile;
    private Set<Resource> customResources = new LinkedHashSet<>();
    private Set<Pair<String, String>> variables = new LinkedHashSet<>();
    private List<Slide> slides = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public File getPresentationFile() { return presentationFile; }
    public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

    public List<Slide> getSlides() { return slides; }
    public void setSlides(List<Slide> slides) { this.slides = slides; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public Set<Resource> getCustomResources() { return customResources; }

    public Set<Pair<String, String>> getVariables() { return this.variables; }
    public void setVariables(Collection<Pair<String, String>> variables) {
        this.variables.clear();
        this.variables.addAll(variables);
    }

    /**
     * Update the thumbnail of a given slide identified by its number.
     * @param slideNumber The number of the slide to update the thumbnail.
     * @param image The new thumbnail.
     * @throws IllegalArgumentException If the slide number is {@code null}.
     */
    public void updateSlideThumbnail(String slideNumber, Image image) {
        if(slideNumber == null) throw new IllegalArgumentException("The slide number can not be null");

        for (Slide s : getSlides()) {
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
    public Slide getSlideByNumber(String slideNumber) {
        Slide slide = null;

        Optional<Slide> slideOpt = slides.stream()
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
    public Slide getSlideById(String id) {
        Slide slide = null;

        Optional<Slide> slideOpt = slides.stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst();

        if(slideOpt.isPresent()) slide = slideOpt.get();

        return slide;
    }

    /**
     * Get the first slide of the presentation. If the presentation has no slides, {@code null} is returned.
     * @return The first slide of the presentation.
     */
    public Slide getFirstSlide() {
        Slide slide = null;

        if(!this.slides.isEmpty()) slide = this.slides.get(0);

        return slide;
    }

    /**
     * Get the last slide of the presentation. If the presentation has no slides, {@code null} is returned.
     * @return The last slide from the presentation.
     */
    public Slide getLastSlide() {
        Slide slide = null;

        if(!this.slides.isEmpty()) slide = this.slides.get(this.slides.size() - 1);

        return slide;
    }

    /**
     * Get the slide before a given slide number. If the slide identified by the given slide number is not found,
     * {@code null} will be returned.
     * @param slideNumber The slide number of the slide to get the previous slide.
     * @return The slide before the given slide number.
     */
    public Slide getSlideBefore(final String slideNumber) {
        Slide slide = null;

        if(!this.slides.isEmpty()) {
            int index = 0;

            while(slide == null && index < this.slides.size()) {
                final Slide currentSlide = this.slides.get(index);

                if(currentSlide.getSlideNumber().equals(slideNumber)) {
                    if (index != 0) slide = this.slides.get(index - 1);
                    break;
                }
                index++;
            }
        }

        return slide;
    }

    /**
     * Update the given {@code slide} in the HTML file. Each {@link SlideElement}
     * of the {@code slide} in the HTML document is updated.
     * If {@link Slide#elements elements}
     * in the given {@code slide} contain variables, their values are inserted in the final HTML document. But the slide
     * will not be updated.
     * If the slide contains variables outside the {@link Slide#elements elements}
     * they will also be replaced in the HTML document.
     * @param slide The slide to update in the HTML document.
     */
    public void updateSlideInDocument(final Slide slide) {
        if(slide == null) throw new IllegalArgumentException("The slide can not be null");

        slide.getElements()
                .stream()
                .forEach(element -> this.document.getElementById(element.getId())
                                                 .html(element.getClearedHtmlContent(this.variables)));
    }
}
