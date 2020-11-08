package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultJvmSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Task creating a Java runtime for the current project.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
@CacheableTask
public class CreateRuntime extends DefaultJvmSlideshowFXTask<PackageExtension> {
    private File runtimeDir;
    private final Property<JavaLauncher> launcher = getProject().getObjects().property(JavaLauncher.class);

    public CreateRuntime() {
        super(PackageExtension.class);

        final var toolchain = getProject().getExtensions().getByType(JavaPluginExtension.class).getToolchain();
        final var service = getProject().getExtensions().getByType(JavaToolchainService.class);
        final var defaultLauncher = service.launcherFor(toolchain);
        launcher.convention(defaultLauncher);
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
        //this.extension.getPlop().getNames().forEach(System.out::println);
        if (this.getRuntimeDir().exists()) {
            getProject().delete(this.getRuntimeDir());
        }

        final List<Object> cmd = new ArrayList<>();
        cmd.add(getJavaBinary("jlink"));

        if (this.extension.runtime.jlinkOptions != null) {
            this.extension.runtime.jlinkOptions.forEach(cmd::add);
        }

        if (this.extension.runtime.modules != null && !this.extension.runtime.modules.isEmpty()) {
            cmd.add("--add-modules");
            cmd.add(this.extension.runtime.modules.stream().collect(joining(",")));
        }

        cmd.add("--output");
        cmd.add(getRuntimeDir());

        getProject().exec(spec -> spec.commandLine(cmd));
    }
}
