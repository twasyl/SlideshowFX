package com.twasyl.slideshowfx.engine.template.configuration;

/**
 *
 * @author Thierry Wayslczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class SlideElementTemplate {
    private int id;
    private String htmlId;
    private String defaultContent;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHtmlId() { return htmlId; }
    public void setHtmlId(String htmlId) { this.htmlId = htmlId; }

    public String getDefaultContent() { return defaultContent; }
    public void setDefaultContent(String defaultContent) { this.defaultContent = defaultContent; }
}
