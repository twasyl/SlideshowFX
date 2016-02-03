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

package com.twasyl.slideshowfx.content.extension.snippet;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.snippet.controllers.SnippetContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The content extension that allows to insert code snippet in a presentation. This extension only supports HTML for
 * inserting the content in the presentation, meaning that HTML code will always be returned when calling
 * {@link #buildDefaultContentString()} and {@link #buildContentString(com.twasyl.slideshowfx.markup.IMarkup)}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SnippetContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(SnippetContentExtension.class.getName());

    private SnippetContentExtensionController controller;

    public SnippetContentExtension() {
        super("SNIPPET",
                SnippetContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/snippet/resources/snippet-executor.zip"),
                FontAwesomeIcon.TERMINAL,
                "Insert an executable code snippet",
                "Insert an executable code snippet");

        final String baseURL = "snippet-executor/";

        // Add URL
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("font-awesome-4.5.0/css/font-awesome.min.css"));
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("prism/prism.css"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("prism/prism.js"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/snippet/fxml/SnippetContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for SnippetContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();

        long id = System.currentTimeMillis();
        final String codeSnippetId= "code-snippet-" + id;
        final String codeSnippetConsoleOuputId= "code-snippet-output-" + id;
        final String codeSnippetConsoleId = "code-snippet-console-" + id;
        final String executeCodeSnippetId = "code-snippet-execute-" + id;

        builder.append("<div style=\"width: 100%; height: 50px; background-color: #ECECEC;")
                .append("border-radius: 10px 10px 0 0\" id=\"").append(codeSnippetId).append("\">\n")
                .append("   <i id=\"").append(executeCodeSnippetId).append("\" class=\"fa fa-terminal fa-fw\" ")
                .append("onclick=\"javascript:executeCodeSnippet('")
                .append(this.controller.getSnippetExecutor().getCode())
                .append("', '").append(Base64.getEncoder().encodeToString(this.controller.getCodeSnippet().toJson().getBytes())).append("', '")
                .append(id).append("');\"></i>\n")
                .append("</div>\n")
                .append("<pre id=\"").append(codeSnippetConsoleId).append("\" style=\"margin-top: 0\" ")
                .append("class=\"").append(this.controller.getSnippetExecutor().getCssClass()).append("\">")
                .append("<code class=\"").append(this.controller.getSnippetExecutor().getCssClass()).append("\">")
                .append(this.controller.getCodeSnippet().getCode())
                .append("</code></pre>\n")
                .append("<pre id=\"").append(codeSnippetConsoleOuputId).append("\" class=\"language-bash\" style=\"display: none; margin-top: 0;\">")
                .append("<code class=\"language-bash\"></code></pre>");
        return builder.toString();
    }
}
