package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.app.SlideshowFX;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.services.AutoSavingService;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.theme.Theme;
import com.twasyl.slideshowfx.theme.ThemeNotFoundException;
import com.twasyl.slideshowfx.theme.Themes;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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
public class OptionsViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(OptionsViewController.class.getName());

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

    /**
     * This methods saves the options displayed in the view and make them persistent.
     */
    public void saveOptions() {
        final OSGiManager manager = OSGiManager.getInstance();
        manager.getInstalledServices(ISnippetExecutor.class)
                .forEach(ISnippetExecutor::saveNewOptions);
        manager.getInstalledServices(IHostingConnector.class)
                .forEach(IHostingConnector::saveNewOptions);

        this.saveAutoSavingOptions();
        this.saveTemporaryFilesDeletion();
        this.saveMaxRecentPresentations();
        this.saveSnapshotDelay();
        this.saveTheme();
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

        final String autoSavingInterval = this.autoSavingInterval.getText();

        if (autoSavingInterval != null) {
            try {
                final Long newAutoSavingInterval = Long.valueOf(autoSavingInterval);
                GlobalConfiguration.setAutoSavingInterval(newAutoSavingInterval);

                AutoSavingService.setDelayForAllServices(newAutoSavingInterval);
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

        final String temporaryFilesMaxAge = this.temporaryFilesMaxAge.getText();

        if (temporaryFilesMaxAge != null) {
            try {
                GlobalConfiguration.setTemporaryFilesMaxAge(Long.parseLong(temporaryFilesMaxAge));
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
        final String maxRecentPresentations = this.maxRecentPresentations.getText();

        if (maxRecentPresentations != null) {
            try {
                GlobalConfiguration.setMaxRecentPresentations(Long.parseLong(maxRecentPresentations));
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
        final String snapshotDelay = this.snapshotDelay.getText();

        if (snapshotDelay != null) {
            try {
                GlobalConfiguration.setSnapshotDelay(Long.parseLong(snapshotDelay));
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

    /**
     * Display the configuration UI for each {@link ISnippetExecutor}.
     */
    private void initializeSnippetExecutorUI() {
        OSGiManager.getInstance().getInstalledServices(ISnippetExecutor.class)
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
        OSGiManager.getInstance().getInstalledServices(IHostingConnector.class)
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
        final Long maxRecentPresentations = GlobalConfiguration.getMaxRecentPresentations();
        this.maxRecentPresentations.setText(String.valueOf(maxRecentPresentations));
    }

    private void initializeSnapshotDelay() {
        final Long snapshotDelay = GlobalConfiguration.getSnapshotDelay();
        this.snapshotDelay.setText(String.valueOf(snapshotDelay));
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
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initializeThemes();
        this.initializeAutoSaveUI();
        this.initializeTemporaryFilesDeletionUI();
        this.initializeSnippetExecutorUI();
        this.initializeHostingConnectorUI();
        this.initializeMaxRecentPresentations();
        this.initializeSnapshotDelay();
    }
}
