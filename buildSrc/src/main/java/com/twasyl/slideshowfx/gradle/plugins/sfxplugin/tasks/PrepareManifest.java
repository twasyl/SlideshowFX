package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

/**
 * Task defining the entries in the {@code MANIFEST.MF} file of the produced JAR file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PrepareManifest extends DefaultSlideshowFXTask<SlideshowFXPluginExtension> {

    public PrepareManifest() {
        super(SlideshowFXPluginExtension.class);
    }

    @TaskAction
    public void prepare() {
        final Jar jar = (Jar) getProject().getTasks().getByName("jar");
        jar.getManifest().attributes(this.extension.buildManifestAttributes(getProject()));
    }
}
