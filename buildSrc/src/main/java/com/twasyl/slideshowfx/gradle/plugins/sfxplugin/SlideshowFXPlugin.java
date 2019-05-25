package com.twasyl.slideshowfx.gradle.plugins.sfxplugin;

import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks.InstallPlugin;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks.PrepareManifest;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks.UninstallAllVersionsPlugin;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks.UninstallPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.jvm.tasks.Jar;

/**
 * Gradle plugin for defining a SlideshowFX plugin. The plugin adds several tasks to the build:
 * <ul>
 *     <li><strong>prepare-manifest</strong> which adds to the MANIFEST.MF file the attribute regarding OSGi. These
 *     attributes are defined by a {@code sfxPlugin { bundle { ... }}}</li>
 *     <li><strong>install-plugin</strong> for installing the current plugin in the {@code $user.home/.SlideshowFX/plugins}
 *     directory</li>
 *     <li><strong>uninstall-plugin</strong> which uninstall the <strong>current</strong> version of the plugin from the
 *     installation directory</li>
 *     <li><strong>uninstall-all-versions-plugin</strong> which uninstall all versions of the plugin</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        final SlideshowFXPluginExtension sfxPlugin = project.getExtensions().create("sfxPlugin", SlideshowFXPluginExtension.class);
        project.getPluginManager().apply(JavaLibraryPlugin.class);

        final Task prepareManifest = project.getTasks().create("prepare-manifest", PrepareManifest.class, sfxPlugin);
        final InstallPlugin installPlugin = project.getTasks().create("install-plugin", InstallPlugin.class);
        project.getTasks().create("uninstall-plugin", UninstallPlugin.class);
        project.getTasks().create("uninstall-all-versions-plugin", UninstallAllVersionsPlugin.class);

        final Jar jar = (Jar) project.getTasks().getByName("jar");
        jar.dependsOn(prepareManifest);

        installPlugin.dependsOn(jar);
    }
}
