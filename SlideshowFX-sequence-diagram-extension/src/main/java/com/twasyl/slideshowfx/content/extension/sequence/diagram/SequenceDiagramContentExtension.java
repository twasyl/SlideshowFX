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

package com.twasyl.slideshowfx.content.extension.sequence.diagram;

import com.twasyl.slideshowfx.content.extension.sequence.diagram.controllers.SequenceDiagramContentExtensionController;
import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.markup.IMarkup;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The content extension that allows to insert sequence diagrams in a presentation. This extension only supports HTML for
 * inserting the content in the presentation, meaning that HTML code will always be returned when calling
 * {@link #buildDefaultContentString()} and {@link #buildContentString(com.twasyl.slideshowfx.markup.IMarkup)}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class SequenceDiagramContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(SequenceDiagramContentExtension.class.getName());

    private SequenceDiagramContentExtensionController controller;

    public SequenceDiagramContentExtension() {
        super("SEQUENCE_DIAGRAM",
                SequenceDiagramContentExtension.class.getResource("/com/twasyl/slideshowfx/content/extension/sequence/diagram/resources/jumly.zip"),
                FontAwesomeIcons.SHARE_ALT_SQUARE,
                "Insert a sequence diagram",
                "Insert a sequence diagram");

        final String baseURL = "jumly/0.2.3/";

        // Add URL
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/jquery-2.1.0.min.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("js/coffee-script-1.7.1.js"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("jumly.min.js"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/sequence/diagram/fxml/SequenceDiagramContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for SequenceDiagramContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder("<script type='text/jumly+sequence'>\n");

        builder.append(this.controller.getSequenceDiagramText())
                .append("\n</script>");

        return builder.toString();
    }
}
