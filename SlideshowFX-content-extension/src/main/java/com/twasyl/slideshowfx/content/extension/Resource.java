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
