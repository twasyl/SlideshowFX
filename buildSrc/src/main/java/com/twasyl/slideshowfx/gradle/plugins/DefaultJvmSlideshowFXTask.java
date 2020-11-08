package com.twasyl.slideshowfx.gradle.plugins;

import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;

import java.io.File;

/**
 * Default class for SlideshowFX gradle plugins using JVM binary . This class contains the extension the plugins use.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public abstract class DefaultJvmSlideshowFXTask<T> extends DefaultSlideshowFXTask<T> {
    protected final Property<JavaLauncher> launcher;

    public DefaultJvmSlideshowFXTask(Class<T> extension) {
        super(extension);

        final var toolchain = getProject().getExtensions().getByType(JavaPluginExtension.class).getToolchain();
        final var service = getProject().getExtensions().getByType(JavaToolchainService.class);
        final var defaultLauncher = service.launcherFor(toolchain);
        this.launcher = getProject().getObjects().property(JavaLauncher.class).convention(defaultLauncher);
    }

    /**
     * Get a desired binary present in the JDK installation.
     *
     * @param binary  The name of the binary to get, e.g. {@code java}.
     * @return The full path (including the binary name) of the desired binary.
     */
    protected String getJavaBinary(final String binary) {
        final var binDir = new File(this.launcher.get().getMetadata().getInstallationPath().getAsFile(), "bin");
        final var tool = new File(binDir, binary);
        return tool.getAbsolutePath();
    }
}