package com.twasyl.slideshowfx.builder;

import com.twasyl.slideshowfx.builder.template.SlideTemplate;
import javafx.scene.image.Image;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Represents a slide of the presentation
 */
public class Slide {
    private SlideTemplate template;
    private String slideNumber;
    private String text;
    private Image thumbnail;

    public Slide() {
    }

    public Slide(String slideNumber) {
        this.slideNumber = slideNumber;
    }

    public Slide(SlideTemplate template, String slideNumber) {
        this.template = template;
        this.slideNumber = slideNumber;
    }

    public SlideTemplate getTemplate() { return template; }
    public void setTemplate(SlideTemplate template) { this.template = template; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSlideNumber() { return slideNumber; }
    public void setSlideNumber(String slideNumber) { this.slideNumber = slideNumber; }

    public Image getThumbnail() { return thumbnail; }
    public void setThumbnail(Image thumbnail) { this.thumbnail = thumbnail; }

    public static void buildContent(StringBuffer buffer, Slide slide) throws IOException, SAXException, ParserConfigurationException {
        buffer.append(slide.getText());
    }
}
