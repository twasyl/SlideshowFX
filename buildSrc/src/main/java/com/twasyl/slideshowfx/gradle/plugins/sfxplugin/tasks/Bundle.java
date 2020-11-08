package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLES_CONFIGURATION_NAME;
import static com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.PLUGIN_DEPENDENCIES_CONFIGURATION_NAME;
import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP;

/**
 * Task creating the bundle of a SlideshowFX plugin.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Bundle extends DefaultSlideshowFXTask<SlideshowFXPluginExtension> {

    public Bundle() {
        super(SlideshowFXPluginExtension.class);
        this.setGroup(BUILD_GROUP);
    }

    @OutputFile
    public File getBundleFile() {
        final File libs = new File(getProject().getBuildDir(), "libs");
        return new File(libs, getProject().getName() + "-" + getProject().getVersion() + ".sfx-plugin");
    }

    @TaskAction
    public void bundle() {
        final Jar jar = (Jar) getProject().getTasks().getByName("jar");

        if (jar.getArchiveFile().isPresent()) {
            try (final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(this.getBundleFile()))) {
                appendFileToArchive(zip, ".", jar.getArchiveFile().get().getAsFile());

                final Configuration compile = getProject().getConfigurations().getByName(PLUGIN_DEPENDENCIES_CONFIGURATION_NAME);
                compile.resolve().stream().forEach(file -> appendFileToArchive(zip, "libs", file));

                getProject().getArtifacts().add(BUNDLES_CONFIGURATION_NAME, getBundleFile());
            } catch (IOException e) {
                throw new GradleException("Error creating bundle", e);
            }
        }
    }

    private void appendFileToArchive(final ZipOutputStream zip, String dir, final File file) {
        try {
            if (".".equals(dir)) {
                zip.putNextEntry(new ZipEntry(file.getName()));
            } else {
                zip.putNextEntry(new ZipEntry(dir + "/" + file.getName()));
            }

            try (final FileInputStream input = new FileInputStream(file)) {
                final byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {
                    zip.write(buffer, 0, bytesRead);
                }

                zip.flush();
            }

            zip.closeEntry();
        } catch (IOException e) {
            getLogger().error("Error append the file " + file.getName() + " to the bundle", e);
        }
    }
}
