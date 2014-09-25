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

package com.twasyl.slideshowfx.content.extension.code;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.code.controllers.CodeContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CodeContentExtension extends the AbstractContentExtension. It allows to build a content containing code to insert
 * inside a SlideshowFX presentation.
 * This extension uses highlightjs in order to manage programming language syntax coloration.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class CodeContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(CodeContentExtension.class.getName());

    private CodeContentExtensionController controller;

    public CodeContentExtension() {
        super("CODE",
                "/com/twasyl/slideshowfx/content/extension/code/",
                CodeContentExtension.class.getResourceAsStream("/com/twasyl/slideshowfx/content/extension/code/images/icon.png"),
                "Insert code",
                "Insert code");

        final String baseURL = "highlightjs/8.2/";

        // Add URL
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("styles/github.css"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("highlight.pack.js"));
        this.putResource(ResourceType.SCRIPT, "hljs.initHighlightingOnLoad();");

        // Add locations
        this.putResourceLocation(baseURL.concat("highlight.pack.js"));
        this.putResourceLocation(baseURL.concat("styles/").concat("arta.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("ascetic.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-dune.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-dune.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-forest.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-forest.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-heath.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-heath.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-lakeside.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-lakeside.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-seaside.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("atelier-seaside.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("brown_paper.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("brown_papersq.png"));
        this.putResourceLocation(baseURL.concat("styles/").concat("codepen-embed.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("color-brewer.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("default.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("docco.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("far.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("foundation.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("github.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("googlecode.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("hybrid.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("idea.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("ir_black.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("kimbie.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("kimbie.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("list.txt"));
        this.putResourceLocation(baseURL.concat("styles/").concat("magula.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("mono-blue.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("monokai.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("monokai_sublime.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("obsidian.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("paraiso.dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("paraiso.light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("pojoaque.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("pojoaque.jpg"));
        this.putResourceLocation(baseURL.concat("styles/").concat("railscasts.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("rainbow.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("school_book.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("school_book.png"));
        this.putResourceLocation(baseURL.concat("styles/").concat("solarized_dark.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("solarized_light.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("sunburst.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("tomorrow-night-blue.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("tomorrow-night-bright.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("tomorrow-night-eighties.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("tomorrow-night.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("tomorrow.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("vs.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("xcode.css"));
        this.putResourceLocation(baseURL.concat("styles/").concat("zenburn.css"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/code/fxml/CodeContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for CodeContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        final StringBuilder builder = new StringBuilder();

        if(markup == null || "HTML".equals(markup.getCode())) {
            builder.append(this.buildDefaultContentString());
        } else if("TEXTILE".equals(markup.getCode())) {
            builder.append("bc(")
                    .append(this.controller.getLanguage().getCssClass())
                    .append("){background-color: ").append(this.controller.getBackgroundColorHexadecimal()).append("; width: 93%;}.. ")
                    .append(this.controller.getCode());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("<pre><code class=\"")
                .append(this.controller.getLanguage().getCssClass())
                .append("\" style=\"background-color: ").append(this.controller.getBackgroundColorHexadecimal()).append("; width: 93%\">")
                .append(this.controller.getCode())
                .append("</code></pre>");

        return builder.toString();
    }
}
