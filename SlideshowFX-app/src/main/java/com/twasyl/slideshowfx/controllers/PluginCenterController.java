package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.plugin.InstalledPlugin;
import com.twasyl.slideshowfx.ui.controls.PluginFileButton;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.PLUGINS_DIRECTORY;

/**
 * Controller of the {@code PluginCenter.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PluginCenterController implements Initializable {

    private static Logger LOGGER = Logger.getLogger(PluginCenterController.class.getName());

    @FXML private TilePane plugins;
    @FXML private Button installPlugin;

    @FXML
    private void dragFilesOverPluginButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();

        if(event.getGestureSource() != this.installPlugin && dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    @FXML
    private void dropFileOverPluginButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        boolean allFilesAreValid;

        if(event.getGestureSource() != this.installPlugin && dragboard.hasFiles()) {
            allFilesAreValid = true;
            File pluginFile;
            int index = 0;

            while(allFilesAreValid && index < dragboard.getFiles().size()) {
                pluginFile = dragboard.getFiles().get(index++);

                allFilesAreValid = this.checkChosenPluginFile(pluginFile);
            }
        } else {
            allFilesAreValid = false;
        }

        event.setDropCompleted(allFilesAreValid);
        event.consume();
    }

    @FXML
    private void choosePlugin(final ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(SlideshowFXExtensionFilter.PLUGIN_FILES);
        File pluginFile = chooser.showOpenDialog(null);

        if(pluginFile != null) {
            this.checkChosenPluginFile(pluginFile);
        }
    }

    /**
     * Checks if a file chosen by the user is valid or not. In case the file is not a valid plugin, then an error
     * message is displayed. If it is valid, the plugin file is added to the list of plugins to install and displayed
     * in the plugins table.
     * @param pluginFile The plugin file to check.
     * @return {@code true} if the file is a valid plugin, {@code false} otherwise.
     */
    protected boolean checkChosenPluginFile(final File pluginFile) {
        boolean valid = false;

        try {
            if(fileSeemsValid(pluginFile)) {
                final PluginFileButton pluginFileButton = new PluginFileButton(pluginFile);
                pluginFileButton.setSelected(true);

                this.plugins.getChildren().add(pluginFileButton);

                valid = true;
            }
            else {
                DialogHelper.showError("Invalid plugin", "The chosen plugin seems invalid");
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Can not determine if the plugin file seems valid", e);
        }

        return valid;
    }

    /**
     * Checks if the given {@link File file} is a plugin seems to be a plugin that can be installed in SlideshowFX.
     * @param file The file to check.
     * @return {@code true} if the file seems to be plugin, {@code false} otherwise.
     * @throws NullPointerException If the file is {@code null}.
     * @throws FileNotFoundException If the file doesn't exist.
     */
    protected boolean fileSeemsValid(final File file) throws FileNotFoundException {
        if(file == null) throw new NullPointerException("The file to check can not be null");
        if(!file.exists()) throw new FileNotFoundException("The file to check must exist");

        boolean isValid = false;

        if(file.getName().endsWith(".jar")) {
            try(final JarFile jar = new JarFile(file)) {
                final Manifest manifest = jar.getManifest();
                final Attributes attributes = manifest.getMainAttributes();

                if(attributes != null) {
                    final String name = attributes.getValue("Bundle-Name");
                    final String version = attributes.getValue("Bundle-Version");
                    final String activator = attributes.getValue("Bundle-Activator");

                    isValid = isManifestAttributeValid(name) && isManifestAttributeValid(version)
                            && isManifestAttributeValid(activator);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Can not create a JarFile instance for the plugin: " + file.getName(), e);
            }
        }

        return isValid;
    }

    /**
     * Check if a given attribute is considered valid. An attribute is considered valid if is is not null and not
     * empty.
     * @param attribute The attribute to check.
     * @return {@code true} if the attribute is valid, {@code false} otherwise.
     */
    protected boolean isManifestAttributeValid(final String attribute) {
        return attribute != null && !attribute.trim().isEmpty();
    }

    protected InstalledPlugin createInstalledPlugin(final File pluginFile) {
        InstalledPlugin plugin = null;

        try(final JarFile jar = new JarFile(pluginFile)) {
            final Manifest manifest = jar.getManifest();
            final Attributes attributes = manifest.getMainAttributes();

            if(attributes != null) {
                final String name = attributes.getValue("Bundle-Name");
                final String version = attributes.getValue("Bundle-Version");

                plugin = new InstalledPlugin(name, version);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not create a JarFile instance for the plugin: " + pluginFile.getName(), e);
        }

        return plugin;
    }

    protected void populatePluginsView() {
        for(File pluginFile : PLUGINS_DIRECTORY.listFiles()) {
            if(pluginFile.getName().endsWith(".jar")) {
                final PluginFileButton button = new PluginFileButton(pluginFile);
                button.setSelected(true);
                this.plugins.getChildren().add(button);
            }
        }
    }

    /**
     * Validate the user's choices: if already installed plugins are no more selected, uninstall them. If newly chosen
     * plugins have been added, install them.
     */
    public void validatePluginsConfiguration() {
        this.plugins.getChildren()
                    .filtered(child -> child instanceof PluginFileButton)
                    .forEach(child -> {
                        final PluginFileButton button = (PluginFileButton) child;

                        if(button.isSelected() && !button.getFile().getParentFile().equals(PLUGINS_DIRECTORY)) {
                            try {
                                OSGiManager.deployBundle(button.getFile());
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Can not install plugin: " + button.getFile().getName(), e);
                            }
                        } else if(!button.isSelected() && button.getFile().getParentFile().equals(PLUGINS_DIRECTORY)) {
                            try {
                                OSGiManager.uninstallBundle(button.getFile());
                            } catch (IOException | BundleException e) {
                                LOGGER.log(Level.SEVERE, "Can not uninstall plugin: " + button.getFile().getName(), e);
                            }
                        }
                     });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.populatePluginsView();
    }
}
