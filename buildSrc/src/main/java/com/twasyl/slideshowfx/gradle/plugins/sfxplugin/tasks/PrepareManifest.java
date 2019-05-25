package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import javax.inject.Inject;

/**
 * Task defining the entries in the {@code MANIFEST.MF} file of the produced JAR file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PrepareManifest extends DefaultTask {

    private SlideshowFXPluginExtension sfxPlugin;

    @Inject
    public PrepareManifest(SlideshowFXPluginExtension sfxPlugin) {
        this.sfxPlugin = sfxPlugin;
    }

    @TaskAction
    public void prepare() {
        final Jar jar = (Jar) getProject().getTasks().getByName("jar");
        jar.getManifest().attributes(sfxPlugin.getBundle().buildManifestAttributes(getProject()));
    }
}
