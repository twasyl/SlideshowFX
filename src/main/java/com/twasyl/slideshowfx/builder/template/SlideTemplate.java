package com.twasyl.slideshowfx.builder.template;

import com.twasyl.slideshowfx.builder.Slide;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Represents a slide defined by the template
 */
public class SlideTemplate {
    private int id;
    private String name;
    private File file;
    private String[] dynamicIds;
    private DynamicAttribute[] dynamicAttributes;

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

    public String[] getDynamicIds() { return dynamicIds; }
    public void setDynamicIds(String[] dynamicIds) { this.dynamicIds = dynamicIds; }

    public DynamicAttribute[] getDynamicAttributes() { return dynamicAttributes; }
    public void setDynamicAttributes(DynamicAttribute[] dynamicAttributes) { this.dynamicAttributes = dynamicAttributes; }

    public static void buildContent(StringBuffer buffer, Slide slide) throws IOException, SAXException, ParserConfigurationException {
        buffer.append(slide.getText());
    }
}
