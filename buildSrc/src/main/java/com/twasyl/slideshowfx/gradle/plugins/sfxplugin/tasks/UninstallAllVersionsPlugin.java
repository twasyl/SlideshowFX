package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Task uninstalling all versions of the plugin from installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UninstallAllVersionsPlugin extends DefaultSlideshowFXTask<SlideshowFXPluginExtension> {

    public UninstallAllVersionsPlugin() {
        super(SlideshowFXPluginExtension.class);
    }

    @TaskAction
    public void uninstall() {
        final Jar jar = (Jar) getProject().getTasks().getByName("jar");
        final FilenameFilter pluginFilenameFilter = (dir, name) -> name.startsWith(jar.getArchiveBaseName().get());

        Arrays.stream(this.extension.getPluginsDir().getAsFile().get().listFiles(pluginFilenameFilter))
                .forEach(getProject()::delete);
    }
}
