package com.twasyl.slideshowfx.gradle.plugins.sfxrelease.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.extensions.ReleaseExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.internal.UpdateProductVersionNumberFileVisitor;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This tasks takes source files containing the next version token defined by the {@link ReleaseExtension#getNextVersionToken()}
 * and replaces it by the {@link ReleaseExtension#getProductVersion()}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class UpdateProductVersionNumber extends DefaultSlideshowFXTask<ReleaseExtension> {

    public UpdateProductVersionNumber() {
        super(ReleaseExtension.class);
        this.setGroup("Release");
        setDescription("Update the product version number in all relevant files.");
    }

    @TaskAction
    public void udpate() {
        final var dirs = new ArrayList<File>();
        dirs.add(new File(getProject().getRootDir(), "buildSrc"));
        dirs.add(new File(getProject().getRootDir(), ".github"));
        dirs.addAll(getProject().getSubprojects().stream().map(Project::getProjectDir).collect(Collectors.toList()));

        dirs.forEach(dir -> {
            try {
                Files.walkFileTree(dir.toPath(), new UpdateProductVersionNumberFileVisitor(getLogger(), this.extension));
            } catch (IOException e) {
                throw new GradleException("Error updating the version number", e);
            }
        });
    }
}
