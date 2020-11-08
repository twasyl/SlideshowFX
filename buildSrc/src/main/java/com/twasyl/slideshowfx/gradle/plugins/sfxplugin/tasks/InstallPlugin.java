package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Task installing the plugin in the installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class InstallPlugin extends DefaultSlideshowFXTask<SlideshowFXPluginExtension> {

    public InstallPlugin() {
        super(SlideshowFXPluginExtension.class);
    }

    @TaskAction
    public void install() {
        if (!this.extension.getPluginsDir().getAsFile().get().exists()) {
            this.extension.getPluginsDir().getAsFile().get().mkdirs();
        }

        final FileCollection files = getProject().getTasks().getByName(BUNDLE_TASK_NAME).getOutputs().getFiles();
        if (!files.isEmpty()) {
            files.forEach(file -> {
                final Path copy = new File(this.extension.getPluginsDir().getAsFile().get(), file.getName()).toPath();
                try {
                    Files.copy(file.toPath(), copy, REPLACE_EXISTING);
                } catch (IOException e) {
                    getLogger().error("Can not install file " + file.getName(), e);
                }
            });
        }
    }
}
