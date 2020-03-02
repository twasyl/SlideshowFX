package com.twasyl.slideshowfx.gradle.plugins.sfxpackager;

import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.RunApplicationExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.CreatePackage;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.CreateRuntime;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.PrepareResources;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.RunApplication;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * <p>Gradle plugin for creating a package for a JavaFX application.</p>
 * <h1>Tasks</h1>
 * <p>The plugin adds several tasks to the build:</p>
 * <ul>
 * <li><strong>{@value #PREPARE_RESOURCES_TASK_NAME}</strong> implemented by the {@link PrepareResources} task.</li>
 * <li><strong>{@value #CREATE_RUNTIME_TASK_NAME}</strong> implemented by the {@link CreateRuntime} task.</li>
 * <li><strong>{@value #CREATE_PACKAGE_TASK_NAME}</strong> implemented by the {@link CreatePackage} task.</li>
 * <li><strong>{@value #RUN_APPLICATION_TASK_NAME}</strong> implemented by the {@link RunApplication} task.</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPackager implements Plugin<Project> {

    public static final String PACKAGING_EXTENSION_NAME = "packaging";
    public static final String RUN_APPLICATION_EXTENSION_NAME = "runApplication";
    public static final String PREPARE_RESOURCES_TASK_NAME = "prepareResources";
    public static final String CREATE_RUNTIME_TASK_NAME = "createRuntime";
    public static final String CREATE_PACKAGE_TASK_NAME = "createPackage";
    public static final String RUN_APPLICATION_TASK_NAME = "runApplication";

    @Override
    public void apply(Project project) {
        final PackageExtension packaging = project.getExtensions().create(PACKAGING_EXTENSION_NAME, PackageExtension.class, project);
        final RunApplicationExtension runApplicationExtension = project.getExtensions().create(RUN_APPLICATION_EXTENSION_NAME, RunApplicationExtension.class);

        final RunApplication runApplication = project.getTasks().create(RUN_APPLICATION_TASK_NAME, RunApplication.class, packaging, runApplicationExtension);

        final PrepareResources prepareResources = project.getTasks().create(PREPARE_RESOURCES_TASK_NAME, PrepareResources.class, packaging);
        final CreateRuntime createRuntime = project.getTasks().create(CREATE_RUNTIME_TASK_NAME, CreateRuntime.class, packaging);
        final CreatePackage createPackage = project.getTasks().create(CREATE_PACKAGE_TASK_NAME, CreatePackage.class, packaging);

        createPackage.dependsOn(prepareResources, createRuntime);
        runApplication.dependsOn(prepareResources);
    }
}
