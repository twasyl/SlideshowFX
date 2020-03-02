package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.services.AutoSavingService;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.style.theme.Theme;
import com.twasyl.slideshowfx.style.theme.ThemeNotFoundException;
import com.twasyl.slideshowfx.style.theme.Themes;
import com.twasyl.slideshowfx.ui.controls.ExtendedTextField;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the controller for the view {@code OptionsView.fxml}.
 *
 * @author Thierry Wasylczenko
 * @version 1.2
 * @since SlideshowFX 1.0
 */
public class OptionsViewController implements ThemeAwareController {
    private static final Logger LOGGER = Logger.getLogger(OptionsViewController.class.getName());

    @FXML
    private TabPane root;
    @FXML
    private VBox snippetExecutorContainer;
    @FXML
    private VBox hostingConnectorContainer;
    @FXML
    private CheckBox enableAutoSaving;
    @FXML
    private TextField autoSavingInterval;
    @FXML
    private CheckBox enableTemporaryFilesDeletion;
    @FXML
    private TextField temporaryFilesMaxAge;
    @FXML
    private TextField maxRecentPresentations;
    @FXML
    private TextField snapshotDelay;
    @FXML
    private ComboBox<Theme> themes;
    @FXML
    private ExtendedTextField httpProxyHost;
    @FXML
    private ExtendedTextField httpProxyPort;
    @FXML
    private ExtendedTextField httpsProxyHost;
    @FXML
    private ExtendedTextField httpsProxyPort;

    /**
     * This methods saves the options displayed in the view and make them persistent.
     */
    public void saveOptions() {
        final PluginManager manager = PluginManager.getInstance();
        manager.getServices(ISnippetExecutor.class)
                .forEach(ISnippetExecutor::saveNewOptions);
        manager.getServices(IHostingConnector.class)
                .forEach(IHostingConnector::saveNewOptions);

        this.saveAutoSavingOptions();
        this.saveTemporaryFilesDeletion();
        this.saveMaxRecentPresentations();
        this.saveSnapshotDelay();
        this.saveTheme();
        this.saveProxyConfiguration();
    }

    /**
     * Saves the options regarding the auto saving configuration.
     */
    private void saveAutoSavingOptions() {
        GlobalConfiguration.enableAutoSaving(this.enableAutoSaving.isSelected());

        if (!this.enableAutoSaving.isSelected()) {
            AutoSavingService.cancelAll();
        } else {
            AutoSavingService.resumeAll();
        }

        final String autoSavingIntervalValue = this.autoSavingInterval.getText();

        if (autoSavingIntervalValue != null) {
            try {
                final Long newAutoSavingIntervalValue = Long.valueOf(autoSavingIntervalValue);
                GlobalConfiguration.setAutoSavingInterval(newAutoSavingIntervalValue);

                AutoSavingService.setDelayForAllServices(newAutoSavingIntervalValue);
            } catch (NumberFormatException ex) {
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

        final String temporaryFilesMaxAgeValue = this.temporaryFilesMaxAge.getText();

        if (temporaryFilesMaxAgeValue != null) {
            try {
                GlobalConfiguration.setTemporaryFilesMaxAge(Long.parseLong(temporaryFilesMaxAgeValue));
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Invalid temporary files max age", ex);
                GlobalConfiguration.removeTemporaryFilesMaxAge();
            }
        } else {
            GlobalConfiguration.removeTemporaryFilesMaxAge();
        }
    }

    /**
     * Saves the option for the maximum recent presentations to display.
     */
    private void saveMaxRecentPresentations() {
        final String maxRecentPresentationsValue = this.maxRecentPresentations.getText();

        if (maxRecentPresentationsValue != null) {
            try {
                GlobalConfiguration.setMaxRecentPresentations(Long.parseLong(maxRecentPresentationsValue));
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Invalid max recent presentations", ex);
                GlobalConfiguration.setMaxRecentPresentations(GlobalConfiguration.getDefaultMaxRecentPresentations());
            }
        } else {
            GlobalConfiguration.setMaxRecentPresentations(GlobalConfiguration.getDefaultMaxRecentPresentations());
        }

        SlideshowFX.getMainController().refreshRecentlyOpenedPresentations();
    }

    /**
     * Saves the option for the cascading snapshot delay.
     */
    private void saveSnapshotDelay() {
        final String snapshotDelayValue = this.snapshotDelay.getText();

        if (snapshotDelayValue != null) {
            try {
                GlobalConfiguration.setSnapshotDelay(Long.parseLong(snapshotDelayValue));
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Invalid snapshot delay", ex);
                GlobalConfiguration.setSnapshotDelay(GlobalConfiguration.getDefaultSnapshotDelay());
            }
        } else {
            GlobalConfiguration.setSnapshotDelay(GlobalConfiguration.getDefaultSnapshotDelay());
        }
    }

    /**
     * Saves the option for the theme.
     */
    private void saveTheme() {
        final Theme theme = this.themes.getSelectionModel().getSelectedItem();

        if (theme != null) {
            GlobalConfiguration.setThemeName(theme.getName());
        } else {
            GlobalConfiguration.setThemeName(GlobalConfiguration.getDefaultThemeName());
        }
    }

    private void saveProxyConfiguration() {
        GlobalConfiguration.setHttpProxyHost(this.httpProxyHost.getText());
        GlobalConfiguration.setHttpsProxyHost(this.httpsProxyHost.getText());

        if (this.httpProxyPort.isValid()) {
            GlobalConfiguration.setHttpProxyPort(Integer.parseInt(this.httpProxyPort.getText()));
        }

        if (this.httpsProxyPort.isValid()) {
            GlobalConfiguration.setHttpsProxyPort(Integer.parseInt(this.httpsProxyPort.getText()));
        }
    }

    /**
     * Display the configuration UI for each {@link ISnippetExecutor}.
     */
    private void initializeSnippetExecutorUI() {
        PluginManager.getInstance().getServices(ISnippetExecutor.class)
                .forEach(snippet -> {
                    final Node configurationUI = snippet.getConfigurationUI();

                    if (configurationUI != null) {
                        final TitledPane pane = new TitledPane(snippet.getLanguage(), configurationUI);
                        pane.setCollapsible(true);
                        pane.setExpanded(false);
                        this.snippetExecutorContainer.getChildren().add(pane);
                    }
                });
    }

    /**
     * Displays the configuration UI for each {@link IHostingConnector}
     */
    private void initializeHostingConnectorUI() {
        PluginManager.getInstance().getServices(IHostingConnector.class)
                .forEach(hostingConnector -> {
                    final Node configurationUI = hostingConnector.getConfigurationUI();

                    if (configurationUI != null) {
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

    /**
     * Initialize the UI for elements addressing the max recent presentations.
     */
    private void initializeMaxRecentPresentations() {
        final Long maxRecentPresentationsValue = GlobalConfiguration.getMaxRecentPresentations();
        this.maxRecentPresentations.setText(String.valueOf(maxRecentPresentationsValue));
    }

    private void initializeSnapshotDelay() {
        final Long snapshotDelayValue = GlobalConfiguration.getSnapshotDelay();
        this.snapshotDelay.setText(String.valueOf(snapshotDelayValue));
    }

    private void initializeThemes() {
        this.themes.setConverter(new StringConverter<Theme>() {
            @Override
            public String toString(Theme theme) {
                if (theme != null) {
                    return theme.getName();
                }
                return null;
            }

            @Override
            public Theme fromString(String themeName) {
                if (themeName != null) {
                    try {
                        return Themes.getByName(themeName);
                    } catch (ThemeNotFoundException e) {
                        LOGGER.log(Level.WARNING, null, e);
                    }
                }
                return null;
            }
        });

        this.themes.getItems().addAll(Themes.read());

        try {
            final Theme theme = Themes.getByName(GlobalConfiguration.getThemeName());
            this.themes.getSelectionModel().select(theme);
        } catch (ThemeNotFoundException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    private void initializeProxyUI() {
        this.httpProxyHost.setText(GlobalConfiguration.getHttpProxyHost());

        Integer port = GlobalConfiguration.getHttpProxyPort();
        if (port != null) {
            this.httpProxyPort.setText(port.toString());
        }
        this.httpsProxyHost.setText(GlobalConfiguration.getHttpsProxyHost());

        port = GlobalConfiguration.getHttpsProxyPort();
        if (port != null) {
            this.httpsProxyPort.setText(port.toString());
        }
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    public ReadOnlyBooleanProperty areInputsValid() {
        final ReadOnlyBooleanWrapper property = new ReadOnlyBooleanWrapper();
        property.bind(this.httpProxyPort.validProperty().and(this.httpsProxyPort.validProperty()));

        return property;
    }

    @Override
    public void postInitialize(URL location, ResourceBundle resources) {
        this.initializeThemes();
        this.initializeProxyUI();
        this.initializeAutoSaveUI();
        this.initializeTemporaryFilesDeletionUI();
        this.initializeSnippetExecutorUI();
        this.initializeHostingConnectorUI();
        this.initializeMaxRecentPresentations();
        this.initializeSnapshotDelay();
    }
}
