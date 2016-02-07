package com.twasyl.slideshowfx.engine.presentation.configuration;

import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents a slide of the presentation. The slide is composed of:
 * <ul>
 *     <li>a {@link SlideTemplate template} with allows to know from which template it has been created ;</li>
 *     <li>an {@link #getId() id} which represents an internal ID for the sliden, not the HTML one ;</li>
 *     <li>a {@link #getSlideNumber() number} ;</li>
 *     <li>a {@link #getThumbnail() thumbnail} which is like a screenshot of the slide in order to display it in
 *     the SlideshowFX' UI ;</li>
 *     <li>a collection of {@link #getElements() elements} which correspond to all dynamic elements of the slide </li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class Slide {
    private static final Logger LOGGER = Logger.getLogger(Slide.class.getName());

    private SlideTemplate template;
    private String id;
    private String slideNumber;
    private Image thumbnail;
    private final Set<SlideElement> elements = new HashSet<>();

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
     * The elements contained in the slide.
     * @return The collection containing the slide elements.
     */
    public Set<SlideElement> getElements() { return elements; }

    /**
     * Search for an {@link SlideElement element} with the given {@code id}.
     * @param id The ID of the element to look for.
     * @return The {@link SlideElement element} with the given ID or {@code null} if it is not found.
     * @throws NullPointerException If the given ID is {@code null}.
     */
    public SlideElement getElement(final String id) throws NullPointerException {
        Optional<SlideElement> element = this.getElements().stream().filter(e -> id.equals(e.getId())).findFirst();
        return element.isPresent() ? element.get() : null;
    }

    /**
     * Update the slide element identified by its {@code elementId} with the provided content.
     * The given content should not be given in Base64.
     * @param elementId The ID of the element to update.
     * @param code The code corresponding to the markup syntax used to define the original content.
     * @param originalContent The original content if the element.
     * @param htmlContent The HTML content of this element.
     * @return The element that has been updated.
     */
    public SlideElement updateElement(String elementId, String code, String originalContent, String htmlContent) {
        SlideElement updatedElement = null;

        Optional<SlideElement> slideElement = getElements().stream()
                                .filter(element -> element.getId().equals(elementId))
                                .findFirst();

        if(slideElement.isPresent()) {
            slideElement.get().setOriginalContentCode(code);
            slideElement.get().setOriginalContent(originalContent);
            slideElement.get().setHtmlContent(htmlContent);

            updatedElement = slideElement.get();
        } else {
            SlideElement se = new SlideElement();
            se.setId(elementId);
            se.setOriginalContentCode(code);
            se.setOriginalContent(originalContent);
            se.setHtmlContent(htmlContent);

            getElements().add(se);

            updatedElement = se;
        }

        return updatedElement;
    }
}
