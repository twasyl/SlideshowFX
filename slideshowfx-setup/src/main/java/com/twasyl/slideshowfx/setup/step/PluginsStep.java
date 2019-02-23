package com.twasyl.slideshowfx.setup.step;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.setup.controllers.PluginsViewController;
import com.twasyl.slideshowfx.setup.exceptions.SetupStepException;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getPluginsDirectory;

/**
 * A step allowing to choose which plugins should be installed.
 * During the {@link #execute()} method, all plugins selected by the user are copied within the SlideshowFX
 * configuration directory identified by the {@link GlobalConfiguration#getPluginsDirectory()}.
 * During the {@link #rollback()} method, all installed plugins are removed.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class PluginsStep extends AbstractSetupStep {
    private static final Logger LOGGER = Logger.getLogger(PluginsStep.class.getName());

    private boolean pluginsDirectoryCreatedDuringSetup = false;

    /**
     * Create an instance of the step.
     *
     * @param pluginsDirectory The directory containing all plugins that can be installed.
     */
    public PluginsStep(final File pluginsDirectory) {
        this.title("Plugins");

        final FXMLLoader loader = new FXMLLoader(PluginsStep.class.getResource("/com/twasyl/slideshowfx/setup/fxml/PluginsView.fxml"));

        try {
            this.view = loader.load();
            this.controller = loader.getController();

            ((PluginsViewController) this.controller).setPluginsDirectory(pluginsDirectory);

            final BooleanBinding markupSelected = ((PluginsViewController) this.controller).numberOfSelectedMarkup().greaterThan(0);
            this.validProperty().bind(markupSelected);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can not initialize the plugin step", e);
        }
    }

    @Override
    public void execute() throws SetupStepException {
        final List<File> pluginsToInstall = ((PluginsViewController) this.controller).getPluginsToInstall();

        if (!pluginsToInstall.isEmpty()) {
            this.pluginsDirectoryCreatedDuringSetup = GlobalConfiguration.createPluginsDirectory();

            pluginsToInstall.forEach(plugin -> {
                try {
                    final Path destination = new File(getPluginsDirectory(), plugin.getName()).toPath();
                    Files.copy(plugin.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Can not copy plugin to plugins directory", e);
                }
            });
        }
    }

    @Override
    public void rollback() throws SetupStepException {
        final List<File> pluginsToInstall = ((PluginsViewController) this.controller).getPluginsToInstall();

        if (!pluginsToInstall.isEmpty()) {
            for (File plugin : pluginsToInstall) {
                try {
                    Files.delete(plugin.toPath());
                } catch (IOException e) {
                    throw new SetupStepException("Can not delete plugin " + plugin.getAbsolutePath(), e);
                }
            }

            if (this.pluginsDirectoryCreatedDuringSetup) {
                try {
                    IOUtils.deleteDirectory(getPluginsDirectory());
                } catch (IOException e) {
                    throw new SetupStepException("Can not delete plugins directory", e);
                }
            }
        }
    }
}
