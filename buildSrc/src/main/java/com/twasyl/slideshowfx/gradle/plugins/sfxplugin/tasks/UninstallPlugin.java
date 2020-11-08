package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Path;

import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME;

/**
 * Task uninstall the current version (defined in the build script) of the plugin from the installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UninstallPlugin extends DefaultSlideshowFXTask<SlideshowFXPluginExtension> {

    public UninstallPlugin() {
        super(SlideshowFXPluginExtension.class);
    }

    @TaskAction
    public void uninstall() {
        final FileCollection files = getProject().getTasks().getByName(BUNDLE_TASK_NAME).getOutputs().getFiles();

        if (!files.isEmpty()) {
            final var pluginsDir = this.extension.getPluginsDir().getAsFile().get();
            files.forEach(file -> {
                final File pluginFile = new File(pluginsDir, file.getName());
                final Path explodedDir = new File(pluginsDir, file.getName().replace(".sfx-plugin", "")).toPath();
                getProject().delete(pluginFile, explodedDir);
            });
        }
    }
}
