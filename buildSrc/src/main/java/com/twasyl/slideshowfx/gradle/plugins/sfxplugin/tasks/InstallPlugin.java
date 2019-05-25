package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Task installing the plugin in the installation directory.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class InstallPlugin extends AbstractPluginTask {

    @TaskAction
    public void install() throws IOException {
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }

        final Jar jar = (Jar) getProject().getTasks().getByName("jar");
        if (jar.getArchiveFile().isPresent()) {
            final Path copy = new File(pluginsDir, jar.getArchiveFileName().get()).toPath();
            Files.copy(jar.getArchiveFile().get().getAsFile().toPath(), copy, REPLACE_EXISTING);
        }
    }
}
