package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.setup.controls.PluginButton;
import com.twasyl.slideshowfx.utils.beans.Wrapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the {PluginsView.xml} file.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class PluginsViewController implements Initializable {

    public class Plugin {
        public File file;
        public String label;
        public String description;
        public Node icon;
    }

    private static final Logger LOGGER = Logger.getLogger(PluginsViewController.class.getName());

    protected final String MARKUP_PLUGINS_DIRECTORY_NAME = "markups";
    protected final String SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME = "executors";
    protected final String CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME = "extensions";
    protected final String HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME = "hostingConnectors";

    @FXML private GridPane markupPlugins;
    @FXML private GridPane contentExtensionPlugins;
    @FXML private GridPane snippetExecutorPlugins;
    @FXML private GridPane hostingConnectorsPlugins;

    @FXML private CheckBox installAllMarkupPlugins;
    @FXML private CheckBox installAllContentExtensionPlugins;
    @FXML private CheckBox installAllSnippetExecutorPlugins;
    @FXML private CheckBox installAllHostingConnectorPlugins;

    private final ObjectProperty<File> pluginsDirectory = new SimpleObjectProperty<>();
    private final List<File> pluginsToInstall = new ArrayList<>();

    /**
     * Get the list of the plugins the user has chosen to install. Each {@link File} corresponds to the plugin file.
     * @return The list of the plugins the user has chosen to install.
     */
    public List<File> getPluginsToInstall() {
        return this.pluginsToInstall;
    }

    /**
     * Set the directory that contains the plugins to be installed. The directory is not the directory of specialized
     * plugins but the directory that contains the other directories containing those specialized plugins.
     * @param directory The directory containing the plugins.
     * @return This instance of the controller.
     */
    public PluginsViewController setPluginsDirectory(final File directory) {
        this.pluginsDirectory.set(directory);
        return this;
    }

    @FXML private void actionOnInstallAllMarkupPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllMarkupPlugins.isSelected(), this.markupPlugins);
    }

    @FXML private void actionOnInstallAllContentExtensionPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllContentExtensionPlugins.isSelected(), this.contentExtensionPlugins);
    }

    @FXML private void actionOnInstallAllSnippetExecutorPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllSnippetExecutorPlugins.isSelected(), this.snippetExecutorPlugins);
    }

    @FXML private void actionOnInstallAllHostingConnectorPlugins(final ActionEvent event) {
        this.actionOnInstallAllPlugins(this.installAllHostingConnectorPlugins.isSelected(), this.hostingConnectorsPlugins);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.pluginsDirectory.addListener((dirValue, newDir, oldDir) -> {
            fillMarkupPluginsView();
            fillContentExtensionPluginsView();
            fillSnippetExecutorPluginsView();
            fillHostingConnectorPluginsView();
        });
    }

    /**
     * Fill the {@link Node} that will list the available markup plugins.
     * @see #fillPluginsView(String, GridPane, CheckBox)
     */
    protected final void fillMarkupPluginsView() {
        this.fillPluginsView(MARKUP_PLUGINS_DIRECTORY_NAME, this.markupPlugins, this.installAllMarkupPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available content extension plugins.
     * @see #fillPluginsView(String, GridPane, CheckBox)
     */
    protected final void fillContentExtensionPluginsView() {
        this.fillPluginsView(CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME, this.contentExtensionPlugins, this.installAllContentExtensionPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available snippet executor plugins.
     * @see #fillPluginsView(String, GridPane, CheckBox)
     */
    protected final void fillSnippetExecutorPluginsView() {
        this.fillPluginsView(SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME, this.snippetExecutorPlugins, this.installAllSnippetExecutorPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available hosting connector plugins.
     * @see #fillPluginsView(String, GridPane, CheckBox)
     */
    protected final void fillHostingConnectorPluginsView() {
        this.fillPluginsView(HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME, this.hostingConnectorsPlugins, this.installAllHostingConnectorPlugins);
    }

    /**
     * Fill the given {@code view} with the plugins contained within the {@code specializedPluginsDirectoryName}.
     * @param specializedPluginsDirectoryName The name of direcotry containing the plugins to list.
     * @param view The view to be filled.
     * @param installAllPluginsBox The checkbox allowing to select/unselect all plugins in the {@code view}.
     */
    protected final void fillPluginsView(final String specializedPluginsDirectoryName, final GridPane view, final CheckBox installAllPluginsBox) {
        final File specializedPluginsDir = new File(this.pluginsDirectory.get(), specializedPluginsDirectoryName);

        view.getChildren().clear();

        final Wrapper<Integer> column = new Wrapper<>(0);
        final Wrapper<Integer> row = new Wrapper<>(0);

        Arrays.stream(specializedPluginsDir.listFiles())
                .filter(file -> file.getName().endsWith(".jar"))
                .map(file -> buildPlugin(file))
                .sorted((plugin1, plugin2) -> plugin1.label.compareTo(plugin2.label))
                .forEach(plugin -> {
                    final PluginButton button = new PluginButton(plugin);
                    button.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
                        if(newSelected) this.pluginsToInstall.add(plugin.file);
                        else this.pluginsToInstall.remove(plugin.file);

                        this.manageCheckBoxStateForPlugins(view, installAllPluginsBox);
                    });

                    view.add(button, column.getValue() % 5, row.getValue());

                    column.setValue(column.getValue() + 1);

                    if(column.getValue() % 5 == 0 && column.getValue() >= 5) {
                        row.setValue(row.getValue() + 1);
                    }
                });
    }

    /**
     * Create an instance of {@link Plugin} corresponding to the given {@code pluginFile}.
     * @param pluginFile The file of the plugin.
     * @return The created plugin.
     */
    protected final Plugin buildPlugin(final File pluginFile) {
        final Attributes manifestAttributes = this.getManifestAttributes(pluginFile);

        final Plugin plugin = new Plugin();
        plugin.file = pluginFile;
        plugin.label = this.getManifestAttributeValue(manifestAttributes, "Setup-Wizard-Label", pluginFile.getName());
        plugin.description = this.getManifestAttributeValue(manifestAttributes, "Bundle-Description", "");
        plugin.icon = this.buildIconNode(pluginFile, manifestAttributes);

        return plugin;
    }

    /**
     * Get the attributes contained in the {@code MANIFEST.MF} of the plugin.
     * @param plugin The JAR file of the plugin.
     * @return The attributes contained in the {@code MANIFEST.MF} file of the plugin.
     */
    protected final Attributes getManifestAttributes(final File plugin) {
        Attributes manifestAttributes = null;

        try {
            final JarFile jarFile = new JarFile(plugin);
            final Manifest manifest = jarFile.getManifest();
            manifestAttributes = manifest.getMainAttributes();
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Can not extract manifest attributes", ex);
        }

        return manifestAttributes;
    }

    /**
     * Get the value of an attribute stored within a collection of {@code attributes}. If the value is {@code null} or
     * empty, the default value will be returned.
     * @param attributes The whole collection of attributes.
     * @param name The name of the attribute to retrieve the value for.
     * @param defaultValue The default value to return if the original value is {@code null} or empty.
     * @return The value of the attribute.
     */
    protected final String getManifestAttributeValue(final Attributes attributes, final String name, final String defaultValue) {
        final String value = attributes.getValue(name);

        if(value == null || value.isEmpty()) return defaultValue;
        else return value;
    }

    /**
     * Get the icon of the plugin stored within the JAR file as an array of bytes. If no icon is present, an empty array
     * is returned.
     * @param plugin The JAR file of the plugin.
     * @return The icon of the plugin.
     */
    protected final byte[] getIconFromJar(final File plugin) {
        final ByteArrayOutputStream iconOut = new ByteArrayOutputStream();

        try {
            final JarFile jarFile = new JarFile(plugin);
            final JarEntry icon = jarFile.getJarEntry("META-INF/icon.png");

            if(icon != null) {
                final InputStream iconIn = jarFile.getInputStream(icon);
                final byte[] buffer = new byte[512];
                int numberOfBytesRead;

                while ((numberOfBytesRead = iconIn.read(buffer)) != -1) {
                    iconOut.write(buffer, 0, numberOfBytesRead);
                }

                iconOut.flush();
                iconOut.close();
            }
        } catch(IOException ex) {
            LOGGER.log(Level.WARNING, "Can not the icon from JAR", ex);
        }

        return iconOut.toByteArray();
    }

    /**
     * Create the {@code Node} that will contain the icon of the plugin.
     * @param plugin The JAR file of the plugin.
     * @param attributes The manifest attributes of the plugin JAR file.
     * @return The element containing the icon of the plugin.
     */
    protected final Node buildIconNode(final File plugin, final Attributes attributes) {
        Node icon = null;
        final byte[] iconFromJar = this.getIconFromJar(plugin);

        if(iconFromJar != null && iconFromJar.length > 0) {
            final ByteArrayInputStream input = new ByteArrayInputStream(iconFromJar);
            final Image image = new Image(input, 50, 50, true, true);
            icon = new ImageView(image);
        } else {
            final String fontIconName = this.getManifestAttributeValue(attributes, "Setup-Wizard-Icon-Name", "");

            if(!fontIconName.isEmpty()) {
                icon = new FontAwesomeIconView(FontAwesomeIcon.valueOf(fontIconName));
                ((FontAwesomeIconView) icon).setGlyphSize(50);
            }
        }

        return icon;
    }

    /**
     * Check or uncheck the given {@code box} according the fact all plugins are selected or not in the given {@code view}.
     * @param view The view determining of the box should be checked or not.
     * @param box The box to check or not.
     */
    protected final void manageCheckBoxStateForPlugins(final GridPane view, final CheckBox box) {
        final Node unselectedNode = view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginButton && !((PluginButton) node).isSelected())
                .findFirst()
                .orElse(null);

        box.setSelected(unselectedNode == null);
    }

    /**
     * Check/Uncheck all plugins in the view according the {@code install} value.
     * @param install Indicates if the plugins should be installed or not.
     * @param view The view to update.
     */
    protected final void actionOnInstallAllPlugins(final boolean install, final GridPane view) {
        view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginButton)
                .map(node -> (PluginButton) node)
                .forEach(plugin -> plugin.setSelected(install));
    }
}
