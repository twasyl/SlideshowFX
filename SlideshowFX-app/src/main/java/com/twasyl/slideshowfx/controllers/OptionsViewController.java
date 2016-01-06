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

package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class is the controller for the view {@code OptionsView.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class OptionsViewController implements Initializable {
    @FXML private VBox snippetExecutorContainer;
    @FXML private VBox hostingConnectorContainer;

    /**
     * This methods saves the options displayed in the view and make them persistent.
     */
    public void saveOptions() {
        OSGiManager.getInstalledServices(ISnippetExecutor.class)
                .forEach(snippet -> snippet.saveNewOptions() );
        OSGiManager.getInstalledServices(IHostingConnector.class)
                .forEach(hostingConnector -> hostingConnector.saveNewOptions() );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /*
         * Display the configuration UI for each ISnippetExecutor.
         */
        OSGiManager.getInstalledServices(ISnippetExecutor.class)
                .forEach(snippet -> {
                    final Node configurationUI = snippet.getConfigurationUI();

                    if(configurationUI != null) this.snippetExecutorContainer.getChildren().add(configurationUI);
                });

        // Displays the configuration UI for each IHostingConnector
        OSGiManager.getInstalledServices(IHostingConnector.class)
                .forEach(hostingConnector -> {
                    final Node configurationUI = hostingConnector.getConfigurationUI();

                    if(configurationUI != null) {
                        final TitledPane pane = new TitledPane(hostingConnector.getName(), configurationUI);
                        pane.setCollapsible(true);
                        pane.setExpanded(false);

                        this.hostingConnectorContainer.getChildren().add(pane);
                    }
                });
    }
}
