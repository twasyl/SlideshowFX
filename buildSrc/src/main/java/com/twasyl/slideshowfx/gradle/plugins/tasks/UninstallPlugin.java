package com.twasyl.slideshowfx.gradle.plugins.tasks;

import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Task uninstall the current version (defined in the build script) of the plugin from the installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UninstallPlugin extends AbstractPluginTask {

    @TaskAction
    public void uninstall() throws IOException {
        final Jar jar = (Jar) getProject().getTasks().getByName("jar");
        final Path plugin = new File(pluginsDir, jar.getArchiveFileName().get()).toPath();
        Files.delete(plugin);
    }
}
