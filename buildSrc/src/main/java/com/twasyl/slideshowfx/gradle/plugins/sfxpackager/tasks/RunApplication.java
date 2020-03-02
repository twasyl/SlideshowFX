package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks;

import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.RunApplicationExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.twasyl.slideshowfx.gradle.Utils.getJavaBinary;
import static com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager.CREATE_RUNTIME_TASK_NAME;
import static com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager.PREPARE_RESOURCES_TASK_NAME;

/**
 * Task running the application the project defines. The run can be done using the runtime created by the {@link CreateRuntime}
 * task by specifying the {@value USE_RUNTIME_PROPERTY} property to the gradle build.
 * <p>
 * In order to start the application in debug mode, two properties can be used:
 *
 * <ul>
 *     <li>{@value DEBUG_PROPERTY} to indicate the application should be run in debug mode</li>
 *     <li>{@value DEBUG_PORT_PROPERTY} to specify the debugging port to use. If not specified, {@code 5005} is used</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RunApplication extends DefaultTask {

    public static final String USE_RUNTIME_PROPERTY = "useRuntime";
    public static final String DEBUG_PROPERTY = "debug";
    public static final String DEBUG_PORT_PROPERTY = "debugPort";

    private PackageExtension packageExtension;
    private RunApplicationExtension runApplicationExtension;

    @Inject
    public RunApplication(final PackageExtension packageExtension, final RunApplicationExtension runApplicationExtension) {
        this.packageExtension = packageExtension;
        this.runApplicationExtension = runApplicationExtension;

        if (getProject().hasProperty(USE_RUNTIME_PROPERTY)) {
            dependsOn(CREATE_RUNTIME_TASK_NAME);
        }
    }

    @TaskAction
    public void run() {
        getProject().exec(spec -> {
            spec.setWorkingDir(this.packageExtension.outputDir);
            spec.commandLine(prepareCommand());
        });
    }

    private List<Object> prepareCommand() {
        final String java;

        if (getProject().hasProperty(USE_RUNTIME_PROPERTY)) {
            final CreateRuntime createRuntime = (CreateRuntime) getProject().getTasks().getByName(CREATE_RUNTIME_TASK_NAME);
            final File binDir = new File(createRuntime.getRuntimeDir(), "bin");
            java = new File(binDir, "java").getAbsolutePath();
        } else {
            java = getJavaBinary(getProject(), "java");
        }

        final List<Object> cmd = new ArrayList<>();
        cmd.add(java);
        cmd.add("--enable-preview");

        if (getProject().hasProperty(DEBUG_PROPERTY)) {
            String debugPort = "5005";
            if (getProject().hasProperty(DEBUG_PORT_PROPERTY)) {
                final String value = getProject().property(DEBUG_PORT_PROPERTY).toString();
                if (!value.isBlank()) {
                    debugPort = value;
                }
            }

            cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:" + debugPort);
        }

        final var prepareResources = (PrepareResources) getProject().getTasks().findByName(PREPARE_RESOURCES_TASK_NAME);
        cmd.add("--module-path");
        cmd.add(prepareResources.getDependenciesDir().getAbsolutePath());

        this.packageExtension.app.jvmOpts.forEach(cmd::add);
        cmd.add("--module");

        if (!this.runApplicationExtension.module.isBlank() && !this.runApplicationExtension.mainClass.isBlank()) {
            cmd.add(this.runApplicationExtension.module + "/" + this.runApplicationExtension.mainClass);
        }

        return cmd;
    }
}
