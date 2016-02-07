package com.twasyl.slideshowfx.content.extension;

/**
 * This class represents a resource of a content extension. It could be a JavaScript file, a CSS file, a JavaScript script
 * or a CSS fragment. It has a content and a type.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class Resource {
    private String content;
    private ResourceType type;

    public Resource(ResourceType type, String content) {
        this.content = content;
        this.type = type;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }


    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    /**
     * This method converts the resource as an HTML string. Typically if the type is of {@code ResourceType.JAVASCRIPT_FILE}
     * it will produces {@code <script type="text/javascript" src="..."></script>}.
     * @param location The location to include in the {@code src} or {@code href} attribute of the HTML string
     * @return The HTML string of the resource.
     */
    public String buildHTMLString(String location) {
        final StringBuilder builder = new StringBuilder();

        if(this.getType() == ResourceType.JAVASCRIPT_FILE) {
            builder.append("<script type=\"text/javascript\" src=\"").append(location).append("/").append(this.getContent()).append("\">");
        } else if(this.getType() == ResourceType.SCRIPT) {
            builder.append("<script type=\"text/javascript\">").append(this.getContent()).append("</script>");
        } else if(this.getType() == ResourceType.CSS_FILE) {
            builder.append("<link rel=\"stylesheet\" href=\"").append(location).append("/").append(this.getContent()).append("\">");
        } else if(this.getType() == ResourceType.CSS) {
            builder.append("<style>").append(this.getContent()).append("</script>");
        }

        return builder.toString();
    }
}
