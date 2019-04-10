package com.twasyl.slideshowfx.engine.presentation.configuration;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.IConfiguration;
import com.twasyl.slideshowfx.utils.beans.Pair;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class PresentationConfiguration implements IConfiguration {
    private static final Logger LOGGER = Logger.getLogger(PresentationConfiguration.class.getName());

    private long id;
    private Document document;
    private File presentationFile;
    private Set<Resource> customResources = new LinkedHashSet<>();
    private Set<Pair<String, String>> variables = new LinkedHashSet<>();
    private List<Slide> slides = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public File getPresentationFile() {
        return presentationFile;
    }

    public void setPresentationFile(File presentationFile) {
        this.presentationFile = presentationFile;
    }

    /**
     * Indicates if the current presentation has slides.
     *
     * @return {@code true} if the presentation has at least one slide returned by {@link #getSlides()}, {@code false} otherwise.
     */
    public boolean hasSlides() {
        return this.slides != null && !this.slides.isEmpty();
    }

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Set<Resource> getCustomResources() {
        return customResources;
    }

    public Set<Pair<String, String>> getVariables() {
        return this.variables;
    }

    public void setVariables(Collection<Pair<String, String>> variables) {
        this.variables.clear();
        this.variables.addAll(variables);
    }

    /**
     * Get a slide by it's slide number.
     *
     * @param slideNumber The slide number og the slide to get.
     * @return The slide or {@code null} if not found.
     */
    public Slide getSlideByNumber(String slideNumber) {
        return slides.stream()
                .filter(s -> slideNumber.equals(s.getSlideNumber()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get a slide by it's ID.
     *
     * @param id The ID of the slide to get.
     * @return The slide or {@code null} if not found.
     */
    public Slide getSlideById(String id) {
        return slides.stream()
                .filter(s -> Objects.equals(id, s.getId()))
                .findAny()
                .orElse(null);
    }

    /**
     * Get the first slide of the presentation. If the presentation has no slides, {@code null} is returned.
     *
     * @return The first slide of the presentation.
     */
    public Slide getFirstSlide() {
        Slide slide = null;

        if (!this.slides.isEmpty()) slide = this.slides.get(0);

        return slide;
    }

    /**
     * Get the last slide of the presentation. If the presentation has no slides, {@code null} is returned.
     *
     * @return The last slide from the presentation.
     */
    public Slide getLastSlide() {
        Slide slide = null;

        if (!this.slides.isEmpty()) slide = this.slides.get(this.slides.size() - 1);

        return slide;
    }

    /**
     * Get the slide before a given slide number. If the slide identified by the given slide number is not found,
     * {@code null} will be returned.
     *
     * @param slideNumber The slide number of the slide to get the previous slide.
     * @return The slide before the given slide number.
     */
    public Slide getSlideBefore(final String slideNumber) {
        return getSiblingSlide(slideNumber, -1);
    }

    /**
     * Get the slide after a given slide number. If the slide identified by the given slide number is not found,
     * {@code null} will be returned.
     *
     * @param slideNumber The slide number of the slide to get the next slide.
     * @return The slide after the given slide number.
     */
    public Slide getSlideAfter(final String slideNumber) {
        return getSiblingSlide(slideNumber, 1);
    }

    /**
     * Get the sibling slide of one identified by the given slide number.
     *
     * @param slideNumber The slide number of the one to get the sibling for.
     * @param delta       {@code 1} to get the next slide, {@code -1} to get the previous slide.
     * @return The slide which is the sibling of the one identified by the given slide number.
     */
    private Slide getSiblingSlide(final String slideNumber, final int delta) {
        Slide slide = null;

        if (!this.slides.isEmpty()) {
            int index = 0;

            while (index < this.slides.size()) {
                final Slide currentSlide = this.slides.get(index);

                if (currentSlide.getSlideNumber().equals(slideNumber)) {
                    try {
                        slide = this.slides.get(delta);
                    } catch (IndexOutOfBoundsException ex) {
                        LOGGER.fine("The slide identified by the slide number " + slideNumber + " doesn't have the desired sibling");
                    }
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
     * If {@link Slide#getElements()}  elements}
     * in the given {@code slide} contain variables, their values are inserted in the final HTML document. But the slide
     * will not be updated.
     * If the slide contains variables outside the {@link Slide#getElements()} elements}
     * they will also be replaced in the HTML document.
     *
     * @param slide The slide to update in the HTML document.
     */
    public void updateSlideInDocument(final Slide slide) {
        if (slide == null) throw new IllegalArgumentException("The slide can not be null");

        slide.getElements()
                .stream()
                .forEach(element -> this.document.getElementById(element.getId())
                        .html(element.getClearedHtmlContent(this.variables)));
    }
}
