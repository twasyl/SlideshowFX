package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import java.io.File;

import static org.gradle.api.plugins.JavaPlugin.JAR_TASK_NAME;
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

/**
 * Task preparing the resources needed for creating a package. The task will copy the dependencies and additional
 * resources needed for the packaging.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PrepareResources extends DefaultSlideshowFXTask<PackageExtension> {
    private File tmpDir;
    private File dependenciesDir;
    private File resourcesDir;
    private File configDir;

    public PrepareResources() {
        super(PackageExtension.class);
        dependsOn(JAR_TASK_NAME);
    }

    private File getTmpDir() {
        if (tmpDir == null) {
            this.tmpDir = new File(getProject().getBuildDir(), "tmp");
        }
        return this.tmpDir;
    }

    @OutputDirectory
    public File getDependenciesDir() {
        if (this.dependenciesDir == null) {
            this.dependenciesDir = new File(this.getTmpDir(), "modules");
        }
        return dependenciesDir;
    }

    @OutputDirectory
    public File getResourcesDir() {
        if (this.resourcesDir == null) {
            this.resourcesDir = new File(this.getTmpDir(), "resources");
        }
        return resourcesDir;
    }

    @OutputDirectory
    public File getConfigDir() {
        if (this.configDir == null) {
            this.configDir = new File(this.getTmpDir(), "config");
        }
        return configDir;
    }

    @TaskAction
    public void copy() {
        copyDependencies();
        copyResources();
    }

    private void copyDependencies() {
        if (!this.getDependenciesDir().exists()) {
            this.getDependenciesDir().mkdirs();
        }

        final var jar = (Jar) getProject().getTasks().getByName(JAR_TASK_NAME);
        if (jar != null && jar.getArchiveFile().isPresent()) {
            getProject().copy(spec -> spec.from(jar.getArchiveFile().get()).into(dependenciesDir));
        }

        final Configuration runtimeClasspath = getProject().getConfigurations().getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        if (runtimeClasspath != null) {
            runtimeClasspath.resolve().forEach(dependency -> getProject().copy(copySpec -> copySpec.from(dependency).into(getDependenciesDir())));
        }
    }

    private void copyResources() {
        if (this.extension.resources != null && !this.extension.resources.isEmpty()) {

            if (!this.getResourcesDir().exists()) {
                this.getResourcesDir().mkdirs();
            }

            this.extension.resources.forEach((from, to) -> getProject().copy(spec -> spec.from(from).into(new File(this.getResourcesDir(), to))));
        }
    }
}
