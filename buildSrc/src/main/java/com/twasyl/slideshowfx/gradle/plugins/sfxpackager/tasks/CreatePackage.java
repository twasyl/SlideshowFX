package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultJvmSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions.PackageExtension;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;

import static com.twasyl.slideshowfx.gradle.Utils.isMac;
import static com.twasyl.slideshowfx.gradle.Utils.isWindows;
import static com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager.CREATE_RUNTIME_TASK_NAME;
import static com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager.PREPARE_RESOURCES_TASK_NAME;
import static java.io.File.separatorChar;
import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP;

/**
 * Task creating the native launcher of the application.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class CreatePackage extends DefaultJvmSlideshowFXTask<PackageExtension> {

    public CreatePackage() {
        super(PackageExtension.class);
        this.setGroup(BUILD_GROUP);
    }

    @TaskAction
    public void create() {
        if (getPackage().exists()) {
            getProject().delete(getPackage());
        }

        final var cmd = new ArrayList<>();
        cmd.add(getJavaBinary("jpackage"));

        final String icon;

        if (isMac()) {
            icon = "macosx" + separatorChar + "icon.icns";
        } else if (isWindows()) {
            icon = "windows" + separatorChar + "icon.ico";
        } else {
            icon = "unix" + separatorChar + "icon.png";
        }

        cmd.add("--type");
        cmd.add("app-image");

        cmd.add("--icon");
        cmd.add(new File(getProject().getProjectDir(), "src" + separatorChar + "assembly" + separatorChar + "javafx" + separatorChar + "package" + separatorChar + icon).getAbsolutePath());

        cmd.add("--dest");
        cmd.add(this.extension.outputDir.getAbsolutePath());

        cmd.add("--name");
        cmd.add(this.extension.executableBaseName);

        final var createRuntimeTask = (CreateRuntime) getProject().getTasks().getByName(CREATE_RUNTIME_TASK_NAME);
        cmd.add("--runtime-image");
        cmd.add(createRuntimeTask.getRuntimeDir());

        final var prepareResourcesTask = (PrepareResources) getProject().getTasks().getByName(PREPARE_RESOURCES_TASK_NAME);
        cmd.add("--module-path");
        cmd.add(prepareResourcesTask.getDependenciesDir().getAbsolutePath());

        cmd.add("--module");
        cmd.add(this.extension.app.module + "/" + this.extension.app.mainClass);

        this.extension.app.jvmOpts.forEach(option -> {
            cmd.add("--java-options");
            cmd.add(option);
        });

        if (this.extension.resources != null && !this.extension.resources.isEmpty()) {
            cmd.add("--input");
            cmd.add(prepareResourcesTask.getResourcesDir().getAbsolutePath());
        }

        getProject().exec(spec -> spec.commandLine(cmd)).assertNormalExitValue();
    }

    @OutputDirectory
    public File getPackage() {
        final String extension = isMac() ? ".app" : "";
        return new File(this.extension.outputDir, this.extension.executableBaseName + extension);
    }

    public String distributionBaseName() {
        final String platform;
        if (isMac()) {
            platform = "osx";
        } else if (isWindows()) {
            platform = "windows";
        } else {
            platform = "unix";
        }

        return extension.executableBaseName + "-" + getProject().getVersion() + "-" + platform;
    }
}
