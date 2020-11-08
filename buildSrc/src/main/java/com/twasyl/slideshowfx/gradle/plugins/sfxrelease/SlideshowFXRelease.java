package com.twasyl.slideshowfx.gradle.plugins.sfxrelease;

import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.extensions.ReleaseExtension;
import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.tasks.RemoveSnapshots;
import com.twasyl.slideshowfx.gradle.plugins.sfxrelease.tasks.UpdateProductVersionNumber;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * This plugin provides methods for managing the release.
 *
 * <h1>Tasks</h1>
 * <ul>
 *     <li>{@link UpdateProductVersionNumber} for updating the version since the files are available in the source files;</li>
 *     <li>{@link RemoveSnapshots} for removing the {@code -SNAPSHOT} version qualifier for subprojects versions.</li>
 * </ul>
 *
 * <h1>Extension</h1>
 * The plugin provides the {@link ReleaseExtension release} extension.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXRelease implements Plugin<Project> {
    private static final String UPDATE_PRODUCT_VERSION_NUMBER_TASK_NAME = "updateProductVersionNumber";
    private static final String REMOVE_SNAPSHOTS_TASK_NAME = "removeSnapshots";
    private static final String RELEASE_EXTENSION_NAME = "release";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(RELEASE_EXTENSION_NAME, ReleaseExtension.class);

        project.getTasks().create(UPDATE_PRODUCT_VERSION_NUMBER_TASK_NAME, UpdateProductVersionNumber.class);
        project.getTasks().create(REMOVE_SNAPSHOTS_TASK_NAME, RemoveSnapshots.class);
    }
}
