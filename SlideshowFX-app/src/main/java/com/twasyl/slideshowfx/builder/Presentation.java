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

import javafx.scene.image.Image;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Represents a presentation
 */
public class Presentation {
    private static final Logger LOGGER = Logger.getLogger(Presentation.class.getName());
    protected static final String PRESENTATION_CONFIGURATION_NAME = "presentation-config.json";
    protected static final String PRESENTATION_FILE_NAME = "presentation.html";

    private Document document;
    private File presentationFile;
    private List<Slide> slides;

    public File getPresentationFile() { return presentationFile; }
    public void setPresentationFile(File presentationFile) { this.presentationFile = presentationFile; }

    public List<Slide> getSlides() { return slides; }
    public void setSlides(List<Slide> slides) { this.slides = slides; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public void updateSlideThumbnail(String slideNumber, Image image) {
        if(slideNumber == null) throw new IllegalArgumentException("The slide number can not be null");

        Slide slideToUpdate = null;
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
     * @param slideNumber
     * @return The slide or null if not found
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
     * @param id
     * @return The slide or null if not found
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
     * In the HTML document update the given slide.
     * @param slide
     */
    public void updateSlideInDocument(Slide slide) {
        if(slide == null) throw new IllegalArgumentException("The slide can not be null");

        slide.getElements().values()
                .stream()
                .forEach(element -> this.document.getElementById(element.getId())
                                                 .html(element.getHtmlContent()));
    }
}
