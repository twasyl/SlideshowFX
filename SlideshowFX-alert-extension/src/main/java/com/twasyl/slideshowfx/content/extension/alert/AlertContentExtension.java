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

package com.twasyl.slideshowfx.content.extension.alert;

import com.twasyl.slideshowfx.content.extension.AbstractContentExtension;
import com.twasyl.slideshowfx.content.extension.ResourceType;
import com.twasyl.slideshowfx.content.extension.alert.controllers.AlertContentExtensionController;
import com.twasyl.slideshowfx.markup.IMarkup;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The AlertContentExtension extends the AbstractContentExtension. It allows to build a content containing alert to insert
 * inside a SlideshowFX presentation.
 * This extension uses sweet-alert in order to manage programming language syntax coloration.
 * This extension supports HTML markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class AlertContentExtension extends AbstractContentExtension {
    private static final Logger LOGGER = Logger.getLogger(AlertContentExtension.class.getName());

    private AlertContentExtensionController controller;

    public AlertContentExtension() {
        super("ALERT",
                "/com/twasyl/slideshowfx/content/extension/alert/",
                AlertContentExtension.class.getResourceAsStream("/com/twasyl/slideshowfx/content/extension/alert/images/icon.png"),
                "Insert an alert",
                "Insert an alert");

        final String baseURL = "sweetalert/0.2.0/";

        // Add URL
        this.putResource(ResourceType.CSS_FILE, baseURL.concat("sweet-alert.css"));
        this.putResource(ResourceType.JAVASCRIPT_FILE, baseURL.concat("sweet-alert.min.js"));
        this.putResource(ResourceType.SCRIPT, "hljs.initHighlightingOnLoad();");

        // Add locations
        this.putResourceLocation(baseURL.concat("sweet-alert.css"));
        this.putResourceLocation(baseURL.concat("sweet-alert.min.js"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-Bold.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-BoldItalic.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-ExtraBold.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-ExtraBoldItalic.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-Italic.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-Light.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-LightItalic.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-regular.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-Semibold.ttf"));
        this.putResourceLocation(baseURL.concat("font/OpenSans-SemiboldItalic.ttf"));
    }

    @Override
    public Pane getUI() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("/com/twasyl/slideshowfx/content/extension/alert/fxml/AlertContentExtension.fxml"));
        Pane root = null;

        try {
            loader.setClassLoader(getClass().getClassLoader());
            root = loader.load();
            this.controller = loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not load UI for AlertContentExtension", e);
        }

        return root;
    }

    @Override
    public String buildContentString(IMarkup markup) {
        return this.buildDefaultContentString().toString();
    }

    @Override
    public String buildDefaultContentString() {
        final StringBuilder builder = new StringBuilder();

        final String id = "swal-btn-" + System.currentTimeMillis();

        builder.append("<button id=\"").append(id).append("\">").append(this.controller.getButtonText()).append("</button>\n");

        builder.append("<script type=\"text/javascript\">\n");
        builder.append("\tdocument.querySelector('#").append(id).append("').onclick = function() {\n");
        builder.append("\t\tswal({\n");
        builder.append("\t\t\ttitle: \"").append(this.controller.getTitle()).append("\"");

        if(this.controller.getText() != null && !this.controller.getText().isEmpty()) {
            builder.append(",\n\t\t\ttext: \"").append(this.controller.getText()).append("\"");
        }

        builder.append(",\n\t\t\ttype: \"").append(this.controller.getType()).append("\",\n");
        builder.append("\t\t\tshowCancelButton: ").append(this.controller.isCancelButtonVisible()).append(",\n");
        builder.append("\t\t\tallowOutsideClick: ").append(this.controller.isClickOutsideAllowed()).append("\n");
        builder.append("\t\t});\n");
        builder.append("\t};\n");
        builder.append("</script>");


        return builder.toString();
    }
}
