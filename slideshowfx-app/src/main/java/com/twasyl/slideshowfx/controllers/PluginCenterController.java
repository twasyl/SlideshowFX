package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import com.twasyl.slideshowfx.plugin.manager.internal.RegisteredPlugin;
import com.twasyl.slideshowfx.ui.controls.PluginFileButton;
import com.twasyl.slideshowfx.utils.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getPluginsDirectory;
import static com.twasyl.slideshowfx.io.SlideshowFXExtensionFilter.PLUGIN_FILES;

/**
 * Controller of the {@code PluginCenter.fxml} view.
 *
 * @author Thierry Wasylczenko
 * @version 1.3-SNAPSHOT
 * @since SlideshowFX 1.1
 */
public class PluginCenterController implements Initializable {

    private static Logger LOGGER = Logger.getLogger(PluginCenterController.class.getName());

    @FXML
    private TilePane plugins;
    @FXML
    private Button installPlugin;

    @FXML
    private void dragFilesOverPluginButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();

        if (event.getGestureSource() != this.installPlugin && dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    }

    @FXML
    private void dropFileOverPluginButton(final DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        boolean allFilesAreValid;

        if (event.getGestureSource() != this.installPlugin && dragboard.hasFiles()) {
            allFilesAreValid = true;
            PluginFile pluginFile;
            int index = 0;

            try {
                while (allFilesAreValid && index < dragboard.getFiles().size()) {
                    pluginFile = new PluginFile(dragboard.getFiles().get(index++));

                    allFilesAreValid = this.checkChosenPluginFile(pluginFile);
                }
            } catch (IOException e) {
                allFilesAreValid = false;
            }
        } else {
            allFilesAreValid = false;
        }

        event.setDropCompleted(allFilesAreValid);
        event.consume();
    }

    @FXML
    private void choosePlugin(final ActionEvent event) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(PLUGIN_FILES);
        File pluginFile = chooser.showOpenDialog(null);

        if (pluginFile != null) {
            this.checkChosenPluginFile(new PluginFile(pluginFile));
        }
    }

    /**
     * Checks if a file chosen by the user is valid or not. In case the file is not a valid plugin, then an error
     * message is displayed. If it is valid, the plugin file is added to the list of plugins to install and displayed
     * in the plugins table.
     *
     * @param pluginFile The plugin file to check.
     * @return {@code true} if the file is a valid plugin, {@code false} otherwise.
     */
    protected boolean checkChosenPluginFile(final PluginFile pluginFile) {
        boolean valid = false;

        try {
            if (fileSeemsValid(pluginFile)) {
                final PluginFileButton pluginFileButton = new PluginFileButton(pluginFile);
                pluginFileButton.setSelected(true);

                this.plugins.getChildren().add(pluginFileButton);

                valid = true;
            } else {
                DialogHelper.showError("Invalid plugin", "The chosen plugin seems invalid");
            }
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Can not determine if the plugin file seems valid", e);
        }

        return valid;
    }

    /**
     * Checks if the given {@link File file} is a plugin seems to be a plugin that can be installed in SlideshowFX.
     *
     * @param file The file to check.
     * @return {@code true} if the file seems to be plugin, {@code false} otherwise.
     * @throws NullPointerException  If the file is {@code null}.
     * @throws FileNotFoundException If the file doesn't exist.
     */
    protected boolean fileSeemsValid(final PluginFile file) throws FileNotFoundException {
        if (file == null) throw new NullPointerException("The file to check can not be null");
        if (!file.exists()) throw new FileNotFoundException("The file to check must exist");

        final RegisteredPlugin plugin = new RegisteredPlugin(file);
        final String name = plugin.getName();
        final String version = plugin.getVersion();

        return isValueValid(name) && isValueValid(version);
    }

    /**
     * Check if a given value is considered valid. A value is considered valid if is is not null and not empty.
     *
     * @param value The attribute to check.
     * @return {@code true} if the value is valid, {@code false} otherwise.
     */
    protected boolean isValueValid(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected void populatePluginsView() {
        for (RegisteredPlugin plugin : PluginManager.getInstance().getActivePlugins()) {
            final PluginFileButton button = new PluginFileButton(plugin.getFile());
            button.setSelected(true);
            this.plugins.getChildren().add(button);
        }
    }

    /**
     * Validate the user's choices: if already installed plugins are no more selected, uninstall them. If newly chosen
     * plugins have been added, install them.
     */
    public void validatePluginsConfiguration() {
        final Predicate<Node> onlyPluginFileButton = child -> child instanceof PluginFileButton;
        final Function<Node, PluginFileButton> toPluginFileButton = child -> (PluginFileButton) child;

        this.plugins.getChildren()
                .stream()
                .filter(onlyPluginFileButton)
                .map(toPluginFileButton)
                .forEach(button -> {
                    if (button.isSelected() && !button.getFile().getParentFile().equals(getPluginsDirectory())) {
                        try {
                            PluginManager.getInstance().installPlugin(button.getFile());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Can not install plugin: " + button.getFile().getName(), e);
                        }
                    } else if (!button.isSelected() && button.getFile().getParentFile().equals(getPluginsDirectory())) {
                        PluginManager.getInstance().uninstallPlugin(button.getFile());
                    }
                });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.populatePluginsView();
    }
}
