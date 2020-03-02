package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks;

import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.twasyl.slideshowfx.gradle.Utils.getJavaBinary;
import static java.util.stream.Collectors.joining;

/**
 * Task creating a Java runtime for the current project.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@CacheableTask
public class CreateRuntime extends DefaultTask {
    private PackageExtension packageExtension;
    private File runtimeDir;

    @Inject
    public CreateRuntime(PackageExtension packageExtension) {
        this.packageExtension = packageExtension;
    }

    @OutputDirectory
    public File getRuntimeDir() {
        if (this.runtimeDir == null) {
            final File tmpDir = new File(getProject().getBuildDir(), "tmp");
            this.runtimeDir = new File(tmpDir, "runtime");
        }
        return runtimeDir;
    }

    @TaskAction
    public void create() {
        if (this.getRuntimeDir().exists()) {
            getProject().delete(this.getRuntimeDir());
        }

        final List<Object> cmd = new ArrayList<>();
        cmd.add(getJavaBinary(getProject(), "jlink"));
        cmd.add("--no-header-files");
        cmd.add("--no-man-pages");
        cmd.add("--compress=2");
        cmd.add("--strip-debug");

        if (this.packageExtension.runtime.modules != null && !this.packageExtension.runtime.modules.isEmpty()) {
            cmd.add("--add-modules");
            cmd.add(this.packageExtension.runtime.modules.stream().collect(joining(",")));
        }

        cmd.add("--output");
        cmd.add(getRuntimeDir());

        getProject().exec(spec -> spec.commandLine(cmd));
    }
}
