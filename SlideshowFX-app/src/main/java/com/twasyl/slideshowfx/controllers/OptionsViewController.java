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

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.services.AutoSavingService;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller for the view {@code OptionsView.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class OptionsViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(OptionsViewController.class.getName());

    @FXML private VBox snippetExecutorContainer;
    @FXML private VBox hostingConnectorContainer;
    @FXML private CheckBox enableAutoSaving;
    @FXML private TextField autoSavingInterval;
    @FXML private CheckBox enableTemporaryFilesDeletion;
    @FXML private TextField temporaryFilesMaxAge;

    /**
     * This methods saves the options displayed in the view and make them persistent.
     */
    public void saveOptions() {
        OSGiManager.getInstalledServices(ISnippetExecutor.class)
                .forEach(snippet -> snippet.saveNewOptions() );
        OSGiManager.getInstalledServices(IHostingConnector.class)
                .forEach(hostingConnector -> hostingConnector.saveNewOptions() );

        this.saveAutoSavingOptions();
        this.saveTemporaryFilesDeletion();
    }

    /**
     * Saves the options regarding the auto saving configuration.
     */
    private void saveAutoSavingOptions() {
        GlobalConfiguration.enableAutoSaving(this.enableAutoSaving.isSelected());

        if(!this.enableAutoSaving.isSelected()) {
            AutoSavingService.cancelAll();
        } else {
            AutoSavingService.resumeAll();
        }

        final String autoSavingInterval = this.autoSavingInterval.getText();

        if(autoSavingInterval != null) {
            try {
                final Long newAutoSavingInterval = Long.valueOf(autoSavingInterval);
                GlobalConfiguration.setAutoSavingInterval(newAutoSavingInterval);

                AutoSavingService.setDelayForAllServices(newAutoSavingInterval);
            } catch(NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Invalid auto saving interval", ex);
                GlobalConfiguration.removeAutoSavingInterval();
            }
        } else {
            GlobalConfiguration.removeAutoSavingInterval();
        }
    }

    /**
     * Saves the options regarding the temporary files deletion configuration.
     */
    private void saveTemporaryFilesDeletion() {
        GlobalConfiguration.enableTemporaryFilesDeletionOnExit(this.enableTemporaryFilesDeletion.isSelected());

        final String temporaryFilesMaxAge = this.temporaryFilesMaxAge.getText();

        if(temporaryFilesMaxAge != null) {
            try {
                GlobalConfiguration.setTemporaryFilesMaxAge(Long.valueOf(temporaryFilesMaxAge));
            } catch(NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Invalid temporary files max age", ex);
                GlobalConfiguration.removeTemporaryFilesMaxAge();
            }
        } else {
            GlobalConfiguration.removeTemporaryFilesMaxAge();
        }
    }

    /**
     * Display the configuration UI for each {@link ISnippetExecutor}.
     */
    private void initializeSnippetExecutorUI() {
        OSGiManager.getInstalledServices(ISnippetExecutor.class)
                .forEach(snippet -> {
                    final Node configurationUI = snippet.getConfigurationUI();

                    if(configurationUI != null) this.snippetExecutorContainer.getChildren().add(configurationUI);
                });
    }

    /**
     * Displays the configuration UI for each {@link IHostingConnector}
     */
    private void initializeHostingConnectorUI() {
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

    /**
     * Initialize the UI for elements addressing the temporary file deletions.
     */
    private void initializeTemporaryFilesDeletionUI() {
        final Long maxAge = GlobalConfiguration.getTemporaryFilesMaxAge();
        this.temporaryFilesMaxAge.setText(maxAge == null ? "" : String.valueOf(maxAge));
        this.temporaryFilesMaxAge.disableProperty().bind(this.enableTemporaryFilesDeletion.selectedProperty().not());

        this.enableTemporaryFilesDeletion.setSelected(GlobalConfiguration.isTemporaryFilesDeletionOnExitEnabled());
    }

    /**
     * Initialize the UI for elements addressing the auto save files.
     */
    private void initializeAutoSaveUI() {
        final Long interval = GlobalConfiguration.getAutoSavingInterval();
        this.autoSavingInterval.setText(interval == null ? "" : String.valueOf(interval));
        this.autoSavingInterval.disableProperty().bind(this.enableAutoSaving.selectedProperty().not());

        this.enableAutoSaving.setSelected(GlobalConfiguration.isAutoSavingEnabled());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.initializeAutoSaveUI();
        this.initializeTemporaryFilesDeletionUI();
        this.initializeSnippetExecutorUI();
        this.initializeHostingConnectorUI();

    }
}
