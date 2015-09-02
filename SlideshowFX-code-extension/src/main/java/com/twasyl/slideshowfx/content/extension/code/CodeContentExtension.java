/*
 * Copyright 2015 Thierry Wasylczenko
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
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The CodeContentExtension extends the AbstractContentExtension. It allows to build a content containing code to insert
 * inside a SlideshowFX presentation.
 * This extension uses PrismJS in order to manage programming language syntax coloration.
 * This extension supports HTML and Textile markup languages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class CodeContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(CodeContentExtension.class.getName());

    private CodeContentExtensionController controller;

    public CodeContentExtension() {
        super("CODE",
                CodeContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/code/resources/prism.zip"),
                FontAwesomeIcon.CODE,
                "Insert code",
                "Insert code");

        final String baseURL = "prism/";

        // Add URL
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("prism.css"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("prism.js"));
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
                    .append(this.controller.getLanguage().getCssClass());

            if(this.controller.isShowingLineNumbers()) {
                builder.append(" line-numbers");
            }

            builder.append(").. ")
                    .append(this.controller.getCode());
        } else {
            builder.append(this.buildDefaultContentString());
        }

        return builder.toString();
    }

    @Override
    public String buildDefaultContentString() {

        final StringBuilder builder = new StringBuilder();
        builder.append("<pre class=\"")
                .append(this.controller.getLanguage().getCssClass());

        if(this.controller.isShowingLineNumbers()) {
            builder.append(" line-numbers");
        }

        builder.append("\"><code class=\"")
                .append(this.controller.getLanguage().getCssClass())
                .append("\">")
                .append(this.controller.getCode())
                .append("</code></pre>");

        return builder.toString();
    }
}
