package com.twasyl.slideshowfx.gradle.plugins.sfxplugin;

import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.tasks.Jar;

import java.io.File;
import java.util.Arrays;

import static com.twasyl.slideshowfx.gradle.annotation.processors.PluginProcessor.GENERATED_DIR_OPTION_NAME;
import static org.gradle.api.plugins.JavaPlugin.*;

/**
 * <p>Gradle plugin for defining a SlideshowFX plugin.</p>
 * <h1>Tasks</h1>
 * <p>The plugin adds several tasks to the build:</p>
 * <ul>
 *     <li><strong>prepare-manifest</strong> which adds to the MANIFEST.MF file the attribute regarding the plugin manager.
 *     These attributes are defined by a {@code sfxPlugin { bundle { ... }}}</li>
 *     <li><strong>install-plugin</strong> for installing the current plugin in the {@code $user.home/.SlideshowFX/plugins}
 *     directory</li>
 *     <li><strong>uninstall-plugin</strong> which uninstall the <strong>current</strong> version of the plugin from the
 *     installation directory</li>
 *     <li><strong>uninstall-all-versions-plugin</strong> which uninstall all versions of the plugin</li>
 * </ul>
 * <h1>Configurations</h1>
 * <p>The plugin creates a configuration named {@value BUNDLES_CONFIGURATION_NAME} where the generated bundle will be added.</p>
 * <p>The plugin creates a configuration named {@value PLUGIN_DEPENDENCIES_CONFIGURATION_NAME} where the dependencies
 * for the plugin must be given.</p>
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPlugin implements Plugin<Project> {

    public static final String SFX_PLUGIN_EXTENSION = "sfxPlugin";
    public static final String BUNDLES_CONFIGURATION_NAME = "bundles";
    public static final String PLUGIN_DEPENDENCIES_CONFIGURATION_NAME = "pluginDependencies";
    public static final String PREPARE_MANIFEST_TASK_NAME = "prepareManifest";
    public static final String BUNDLE_TASK_NAME = "bundle";
    public static final String INSTALL_PLUGIN_TASK_NAME = "installPlugin";
    public static final String UNINSTALL_PLUGIN_TASK_NAME = "uninstallPlugin";
    public static final String UNINSTALL_ALL_VERSIONS_PLUGIN_TASK_NAME = "uninstallAllVersionsPlugin";

    @Override
    public void apply(Project project) {
        createConfigurations(project);
        setupAnnotationProcessing(project);
        project.getPluginManager().apply(JavaLibraryPlugin.class);
        createAndUpdateTasks(project);
        defineProjectDependencies(project);
    }

    private void createConfigurations(Project project) {
        project.getConfigurations().create(BUNDLES_CONFIGURATION_NAME);

        project.getPlugins().withType(JavaLibraryPlugin.class, javaLibraryPlugin -> {
            project.getConfigurations().create(PLUGIN_DEPENDENCIES_CONFIGURATION_NAME, configuration ->
                    project.getConfigurations().getByName(COMPILE_CLASSPATH_CONFIGURATION_NAME).extendsFrom(configuration));
        });
    }

    private void setupAnnotationProcessing(Project project) {
        project.getPlugins().withType(JavaLibraryPlugin.class, javaLibraryPlugin -> {
            final File processor = new File(project.getRootDir(), "buildSrc/build/libs/buildSrc.jar");
            project.getDependencies().add(ANNOTATION_PROCESSOR_CONFIGURATION_NAME, project.files(processor));

            project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> {
                final File generatedDir = new File(project.getBuildDir(), "resources/main");
                javaCompile.getOptions().getCompilerArgs().addAll(Arrays.asList(
                        "-A" + GENERATED_DIR_OPTION_NAME + "=" + generatedDir.getAbsolutePath()));
            });
        });
    }

    private void createAndUpdateTasks(Project project) {
        final TaskContainer tasks = project.getTasks();

        final SlideshowFXPluginExtension sfxPlugin = project.getExtensions().create(SFX_PLUGIN_EXTENSION, SlideshowFXPluginExtension.class);
        final Task prepareManifest = tasks.create(PREPARE_MANIFEST_TASK_NAME, PrepareManifest.class, sfxPlugin);
        final Bundle bundle = tasks.create(BUNDLE_TASK_NAME, Bundle.class);
        final InstallPlugin installPlugin = tasks.create(INSTALL_PLUGIN_TASK_NAME, InstallPlugin.class);
        tasks.create(UNINSTALL_PLUGIN_TASK_NAME, UninstallPlugin.class);
        tasks.create(UNINSTALL_ALL_VERSIONS_PLUGIN_TASK_NAME, UninstallAllVersionsPlugin.class);

        final Jar jar = (Jar) tasks.getByName(JAR_TASK_NAME);
        jar.dependsOn(prepareManifest);
        bundle.dependsOn(jar);
        installPlugin.dependsOn(bundle);
    }

    private void defineProjectDependencies(final Project project) {
        project.afterEvaluate(p -> {
            final SlideshowFXPluginExtension extension = p.getExtensions().getByType(SlideshowFXPluginExtension.class);
            String dependentProject;

            if (extension.isMarkupPlugin()) {
                dependentProject = ":slideshowfx-markup";
            } else if (extension.isContentExtension()) {
                dependentProject = ":slideshowfx-content-extension";
            } else if (extension.isHostingConnector()) {
                dependentProject = ":slideshowfx-hosting-connector";
            } else if (extension.isSnippetExecutor()) {
                dependentProject = ":slideshowfx-snippet-executor";
            } else {
                dependentProject = null;
            }

            if (dependentProject != null) {
                p.getDependencies().add(IMPLEMENTATION_CONFIGURATION_NAME, p.project(dependentProject));
            }
        });
    }
}
