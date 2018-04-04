package com.twasyl.slideshowfx.content.extension;

import java.net.URL;
import java.util.Objects;

/**
 * This class represents a resource of a content extension. It could be a JavaScript file, a CSS file, a JavaScript script
 * or a CSS fragment. It has a content and a type.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class Resource {
    private String content;
    private ResourceType type;
    private ResourceLocation location;
    private URL resourceUrl;

    public Resource(ResourceType type, String content) {
        this(type, content, ResourceLocation.INTERNAL, null);
    }

    public Resource(final ResourceType type, final String content, final ResourceLocation location, final URL resourceUrl) {
        this.content = content;
        this.type = type;
        this.location = location;
        this.resourceUrl = resourceUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    public URL getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(URL resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    /**
     * This method converts the resource as an HTML string. Typically if the type is of {@code ResourceType.JAVASCRIPT_FILE}
     * it will produces {@code <script type="text/javascript" src="..."></script>}.
     *
     * @param location The location to include in the {@code src} or {@code href} attribute of the HTML string
     * @return The HTML string of the resource.
     */
    public String buildHTMLString(String location) {
        final StringBuilder builder = new StringBuilder();

        if (this.getType() == ResourceType.JAVASCRIPT_FILE) {
            builder.append("<script type=\"text/javascript\" src=\"").append(location).append("/").append(this.getContent()).append("\">");
        } else if (this.getType() == ResourceType.SCRIPT) {
            builder.append("<script type=\"text/javascript\">").append(this.getContent()).append("</script>");
        } else if (this.getType() == ResourceType.CSS_FILE) {
            builder.append("<link rel=\"stylesheet\" href=\"").append(location).append("/").append(this.getContent()).append("\">");
        } else if (this.getType() == ResourceType.CSS) {
            builder.append("<style>").append(this.getContent()).append("</style>");
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(content, resource.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
