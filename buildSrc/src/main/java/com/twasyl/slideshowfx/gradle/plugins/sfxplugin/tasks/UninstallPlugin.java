package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.io.IoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME;

/**
 * Task uninstall the current version (defined in the build script) of the plugin from the installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UninstallPlugin extends AbstractPluginTask {

    @TaskAction
    public void uninstall() {
        final FileCollection files = getProject().getTasks().getByName(BUNDLE_TASK_NAME).getOutputs().getFiles();

        if (!files.isEmpty()) {
            files.forEach(file -> {
                final File pluginFile = new File(pluginsDir, file.getName());
                final Path explodedDir = new File(pluginsDir, file.getName().replace(".sfx-plugin", "")).toPath();
                getProject().delete(pluginFile, explodedDir);
            });
        }
    }
}
