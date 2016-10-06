package com.twasyl.slideshowfx.setup.controllers;

import com.twasyl.slideshowfx.ui.controls.PluginFileButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

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
 * @version 1.1
 */
public class PluginsViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(PluginsViewController.class.getName());

    protected static final PseudoClass INVALID_STATE = PseudoClass.getPseudoClass("invalid");

    protected final String MARKUP_PLUGINS_DIRECTORY_NAME = "markups";
    protected final String SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME = "executors";
    protected final String CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME = "extensions";
    protected final String HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME = "hostingConnectors";

    @FXML private TitledPane markupPluginsContainer;
    @FXML private FontAwesomeIconView markupErrorSign;

    @FXML private TilePane markupPlugins;
    @FXML private TilePane contentExtensionPlugins;
    @FXML private TilePane snippetExecutorPlugins;
    @FXML private TilePane hostingConnectorsPlugins;

    @FXML private CheckBox installAllMarkupPlugins;
    @FXML private CheckBox installAllContentExtensionPlugins;
    @FXML private CheckBox installAllSnippetExecutorPlugins;
    @FXML private CheckBox installAllHostingConnectorPlugins;

    private final ObjectProperty<File> pluginsDirectory = new SimpleObjectProperty<>();
    private final List<File> pluginsToInstall = new ArrayList<>();
    private final IntegerProperty numberOfSelectedMarkup = new SimpleIntegerProperty();

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

    /**
     * Return the number of markup plugins that are selected in the view.
     * @return The property indicating the number of markup plugins selected in the view.
     */
    public IntegerProperty numberOfSelectedMarkup() { return this.numberOfSelectedMarkup; }

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
        this.numberOfSelectedMarkup.addListener((value, oldNumber, newNumber) -> {
            if(newNumber.intValue() == 0) this.makeMarkupPluginsContainerInvalid();
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

    protected final void makeMarkupPluginsContainerInvalid() {
        this.markupPluginsContainer.pseudoClassStateChanged(INVALID_STATE, true);
        this.markupPluginsContainer.setTooltip(new Tooltip("At least one markup plugin must be selected"));
        this.markupErrorSign.setVisible(true);
    }

    protected final void makeMarkupPluginsContainerValid() {
        this.markupPluginsContainer.pseudoClassStateChanged(INVALID_STATE, false);
        this.markupPluginsContainer.setTooltip(null);
        this.markupErrorSign.setVisible(false);
    }

    /**
     * Fill the {@link Node} that will list the available markup plugins.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     * @return An {@link IntegerProperty} indicating the number of selected markup plugins in the view.
     */
    protected final IntegerProperty fillMarkupPluginsView() {
        return this.fillPluginsView(MARKUP_PLUGINS_DIRECTORY_NAME, this.markupPlugins, this.installAllMarkupPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available content extension plugins.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     * @return An {@link IntegerProperty} indicating the number of selected content extension plugins in the view.
     */
    protected final IntegerProperty fillContentExtensionPluginsView() {
        return this.fillPluginsView(CONTENT_EXTENSION_PLUGINS_DIRECTORY_NAME, this.contentExtensionPlugins, this.installAllContentExtensionPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available snippet executor plugins.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     * @return An {@link IntegerProperty} indicating the number of selected snippet executor plugins in the view.
     */
    protected final IntegerProperty fillSnippetExecutorPluginsView() {
        return this.fillPluginsView(SNIPPET_EXECUTORS_PLUGINS_DIRECTORY_NAME, this.snippetExecutorPlugins, this.installAllSnippetExecutorPlugins);
    }

    /**
     * Fill the {@link Node} that will list the available hosting connector plugins.
     * @see #fillPluginsView(String, TilePane, CheckBox)
     * @return An {@link IntegerProperty} indicating the number of selected hosting connector plugins in the view.
     */
    protected final IntegerProperty fillHostingConnectorPluginsView() {
        return this.fillPluginsView(HOSTING_CONNECTOR_PLUGINS_DIRECTORY_NAME, this.hostingConnectorsPlugins, this.installAllHostingConnectorPlugins);
    }

    /**
     * Fill the given {@code view} with the plugins contained within the {@code specializedPluginsDirectoryName}.
     * @param specializedPluginsDirectoryName The name of directory containing the plugins to list.
     * @param view The view to be filled.
     * @param installAllPluginsBox The checkbox allowing to select/unselect all plugins in the {@code view}.
     * @return An {@link IntegerProperty} indicating the number of selected plugins in the view.
     */
    protected final IntegerProperty fillPluginsView(final String specializedPluginsDirectoryName, final TilePane view, final CheckBox installAllPluginsBox) {
        final IntegerProperty numberOfSelectedPlugins = new SimpleIntegerProperty(0);
        final File specializedPluginsDir = new File(this.pluginsDirectory.get(), specializedPluginsDirectoryName);

        view.getChildren().clear();

        Arrays.stream(specializedPluginsDir.listFiles())
                .filter(file -> file.getName().endsWith(".jar"))
                .map(file -> new PluginFileButton(file))
                .sorted((button1, button2) -> button1.getLabel().compareTo(button2.getLabel()))
                .forEach(button -> {
                    button.selectedProperty().addListener((selectedValue, oldSelected, newSelected) -> {
                        if(newSelected) {
                            this.pluginsToInstall.add(button.getFile());
                            numberOfSelectedPlugins.set(numberOfSelectedPlugins.get() + 1);
                        }
                        else {
                            this.pluginsToInstall.remove(button.getFile());
                            if(numberOfSelectedPlugins.get() > 0) {
                                numberOfSelectedPlugins.set(numberOfSelectedPlugins.get() - 1);
                            }
                        }

                        this.manageCheckBoxStateForPlugins(view, installAllPluginsBox);
                    });

                    view.getChildren().add(button);
                });

        return numberOfSelectedPlugins;
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
    protected final void manageCheckBoxStateForPlugins(final TilePane view, final CheckBox box) {
        final Node unselectedNode = view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginFileButton && !((PluginFileButton) node).isSelected())
                .findFirst()
                .orElse(null);

        box.setSelected(unselectedNode == null);
    }

    /**
     * Check/Uncheck all plugins in the view according the {@code install} value.
     * @param install Indicates if the plugins should be installed or not.
     * @param view The view to update.
     */
    protected final void actionOnInstallAllPlugins(final boolean install, final TilePane view) {
        view.getChildren()
                .stream()
                .filter(node -> node instanceof PluginFileButton)
                .map(node -> (PluginFileButton) node)
                .forEach(plugin -> plugin.setSelected(install));
    }
}
