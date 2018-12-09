package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.ui.controls.PluginFileButton;
import com.twasyl.slideshowfx.utils.Jar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.TilePane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.icons.Icon.REFRESH;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toList;

/**
 * Controller for the {@code PluginsView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @version 1.3
 * @since SlideshowFX 1.0
 */
public class PluginsViewController implements Initializable {
    private static Logger LOGGER = Logger.getLogger(PluginsViewController.class.getName());
    protected static final PseudoClass INVALID_STATE = PseudoClass.getPseudoClass("invalid");

    protected final String MARKUP_PLUGINS_DIRECTORY_NAME = "markups";
    protected final String SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME = "executors";
    protected final String CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME = "extensions";
    protected final String HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME = "hostingConnectors";

    @FXML
    private TitledPane markupPluginsContainer;
    @FXML
    private FontAwesome markupErrorSign;

    @FXML
    private TilePane markupPlugins;
    @FXML
    private TilePane contentExtensionPlugins;
    @FXML
    private TilePane snippetExecutorPlugins;
    @FXML
    private TilePane hostingConnectorsPlugins;

    @FXML
    private CheckBox installAllMarkupPlugins;
    @FXML
    private CheckBox installAllContentExtensionPlugins;
    @FXML
    private CheckBox installAllSnippetExecutorPlugins;
    @FXML
    private CheckBox installAllHostingConnectorPlugins;

    private List<Jar> installedPlugins;
    private final ObjectProperty<File> pluginsDirectory = new SimpleObjectProperty<>();
    private final List<File> pluginsToInstall = new ArrayList<>();
    private final IntegerProperty numberOfSelectedMarkup = new SimpleIntegerProperty();

    /**
     * Converts the given instance of {@link File} to a {@link Jar} instance.
     *
     * @param file The file to convert.
     * @return An instance of {@link Jar}.
     */
    private static Jar convertFileToJar(File file) {
        if (file != null) {
            try {
                return new Jar(file);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not convert file to Jar", e);
            }
        }
        return null;
    }

    /**
     * Get the list of the plugins the user has chosen to install. Each {@link File} corresponds to the plugin file.
     *
     * @return The list of the plugins the user has chosen to install.
     */
    public List<File> getPluginsToInstall() {
        return this.pluginsToInstall;
    }

    /**
     * Set the directory that contains the plugins to be installed. The directory is not the directory of specialized
     * plugins but the directory that contains the other directories containing those specialized plugins.
     *
     * @param directory The directory containing the plugins.
     * @return This instance of the controller.
     */
    public PluginsViewController setPluginsDirectory(final File directory) {
        this.pluginsDirectory.set(directory);
        return this;
    }

    /**
     * Return the number of markup plugins that are selected in the view.
     *
     * @return The property indicating the number of markup plugins selected in the view.
     */
    public IntegerProperty numberOfSelectedMarkup() {
        return this.numberOfSelectedMarkup;
    }

    @FXML
    private void actionOnInstallAllMarkupPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllMarkupPlugins.isSelected(), this.markupPlugins);
    }

    @FXML
    private void actionOnInstallAllContentExtensionPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllContentExtensionPlugins.isSelected(), this.contentExtensionPlugins);
    }

    @FXML
    private void actionOnInstallAllSnippetExecutorPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllSnippetExecutorPlugins.isSelected(), this.snippetExecutorPlugins);
    }

    @FXML
    private void actionOnInstallAllHostingConnectorPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllHostingConnectorPlugins.isSelected(), this.hostingConnectorsPlugins);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (GlobalConfiguration.getPluginsDirectory().exists()) {
            final File[] jarArray = GlobalConfiguration.getPluginsDirectory()
                    .listFiles((dir, name) -> name != null && !name.startsWith(".") && name.endsWith(".jar"));

            installedPlugins = Arrays.stream(jarArray)
                    .map(PluginsViewController::convertFileToJar)
                    .filter(Objects::nonNull)
                    .collect(toList());
        }

        this.numberOfSelectedMarkup.addListener((value, oldNumber, newNumber) -> {
            if (newNumber.intValue() == 0) this.makeMarkupPluginsContainerInvalid();
            else this.makeMarkupPluginsContainerValid();
        });

        this.makeMarkupPluginsContainerInvalid();

        this.pluginsDirectory.addListener((dirValue, newDir, oldDir) -> {
            this.numberOfSelectedMarkup.bind(fillMarkupPluginsView());
            fillContentExtensionPluginsView();
            fillSnippetExecutorPluginsView();
            fillHostingConnectorPluginsView();
        });
    }

    private final void makeMarkupPluginsContainerInvalid() {
        this.markupPluginsContainer.pseudoClassStateChanged(INVALID_STATE, true);
        this.markupPluginsContainer.setTooltip(new Tooltip("At least one markup plugin must be selected"));
        this.markupErrorSign.setVisible(true);
    }

    private final void makeMarkupPluginsContainerValid() {
        this.markupPluginsContainer.pseudoClassStateChanged(INVALID_STATE, false);
        this.markupPluginsContainer.setTooltip(null);
        this.markupErrorSign.setVisible(false);
    }

    /**
     * Fill the {@link Node} that will list the available markup plugins.
     *
     * @return An {@link IntegerProperty} indicating the number of selected markup plugins in the view.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     */
    private final IntegerProperty fillMarkupPluginsView() {
        return this.fillPluginsView(MARKUP_PLUGINS_DIRECTORY_NAME, this.markupPlugins, this.installAllMarkupPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available content extension plugins.
     *
     * @return An {@link IntegerProperty} indicating the number of selected content extension plugins in the view.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     */
    private final IntegerProperty fillContentExtensionPluginsView() {
        return this.fillPluginsView(CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME, this.contentExtensionPlugins, this.installAllContentExtensionPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available snippet executor plugins.
     *
     * @return An {@link IntegerProperty} indicating the number of selected snippet executor plugins in the view.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     */
    private final IntegerProperty fillSnippetExecutorPluginsView() {
        return this.fillPluginsView(SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME, this.snippetExecutorPlugins, this.installAllSnippetExecutorPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available hosting connector plugins.
     *
     * @return An {@link IntegerProperty} indicating the number of selected hosting connector plugins in the view.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     */
    private final IntegerProperty fillHostingConnectorPluginsView() {
        return this.fillPluginsView(HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME, this.hostingConnectorsPlugins, this.installAllHostingConnectorPlugins);
    }

    /**
     * Fill the given {@code view} with the plugins contained within the {@code specializedPluginsDirectoryName}.
     *
     * @param specializedPluginsDirectoryName The name of directory containing the plugins to list.
     * @param view                            The view to be filled.
     * @param installAllPluginsBox            The checkbox allowing to select/unselect all plugins in the {@code view}.
     * @return An {@link IntegerProperty} indicating the number of selected plugins in the view.
     */
    private final IntegerProperty fillPluginsView(final String specializedPluginsDirectoryName, final TilePane view, final CheckBox installAllPluginsBox) {
        final IntegerProperty numberOfSelectedPlugins = new SimpleIntegerProperty(0);
        final File specializedPluginsDir = new File(this.pluginsDirectory.get(), specializedPluginsDirectoryName);

        view.getChildren().clear();

        Arrays.stream(specializedPluginsDir.listFiles())
                .filter(file -> file.getName().endsWith(".jar"))
                .map(PluginFileButton::new)
                .sorted(Comparator.comparing(PluginFileButton::getLabel))
                .forEach(button -> {
                    button.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
                        if (newSelected) {
                            this.pluginsToInstall.add(button.getFile());
                            numberOfSelectedPlugins.set(numberOfSelectedPlugins.get() + 1);
                        } else {
                            this.pluginsToInstall.remove(button.getFile());
                            if (numberOfSelectedPlugins.get() > 0) {
                                numberOfSelectedPlugins.set(numberOfSelectedPlugins.get() - 1);
                            }
                        }

                        this.manageCheckBoxStateForPlugins(view, installAllPluginsBox);
                    });

                    adaptPluginFileButton(button);
                    view.getChildren().add(button);
                });

        return numberOfSelectedPlugins;
    }

    /**
     * Adapt the given {@link PluginFileButton} to be displayed in a plugins view. This method will checks if the given plugin
     * represented by it's button is already installed in an earlier version. In case the given plugin is newer than
     * the already installed one, then a badge will be displayed on it in order to notify the user that a new version
     * is available.
     *
     * @param button The button of the plugin.
     */
    private void adaptPluginFileButton(final PluginFileButton button) {
        if (isEarlierPluginVersionInstalled(button.getFile())) {
            button.appendBadge(REFRESH, "New plugin version compared to currently installed plugin");
        }
    }

    /**
     * Checks if the given {@link File plugin} is already installed.
     *
     * @param plugin The plugin file to check.
     * @return {@code true} if the plugin is already installed, {@code false} otherwise.
     */
    private boolean isEarlierPluginVersionInstalled(final File plugin) {
        try {
            final Jar jar = new Jar(plugin);
            final String pluginLabel = jar.getManifestAttributeValue("Setup-Wizard-Label", "");
            final String pluginVersion = jar.getManifestAttributeValue("Bundle-Version", "");

            return installedPlugins.stream()
                    .filter(p -> {
                        final String label = p.getManifestAttributeValue("Setup-Wizard-Label", "");
                        final String version = p.getManifestAttributeValue("Bundle-Version", "");

                        return pluginLabel.equals(label) && pluginVersion.compareTo(version) > 0;
                    })
                    .count() > 0;
        } catch (IOException e) {
            LOGGER.log(WARNING, "Error when trying to determine if an older version of plugin is already installed", e);
        }

        return false;
    }


    /**
     * Check or uncheck the given {@code box} according the fact all plugins are selected or not in the given {@code view}.
     *
     * @param view The view determining of the box should be checked or not.
     * @param box  The box to check or not.
     */
    private final void manageCheckBoxStateForPlugins(final TilePane view, final CheckBox box) {
        final Node unselectedNode = view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginFileButton && !((PluginFileButton) node).isSelected())
                .findFirst()
                .orElse(null);

        box.setSelected(unselectedNode == null);
    }

    /**
     * Check/Uncheck all plugins in the view according the {@code install} value.
     *
     * @param install Indicates if the plugins should be installed or not.
     * @param view    The view to update.
     */
    private final void actionOnInstallAllPlugins(final boolean install, final TilePane view) {
        view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginFileButton)
                .map(node -> (PluginFileButton) node)
                .forEach(plugin -> plugin.setSelected(install));
    }
}
