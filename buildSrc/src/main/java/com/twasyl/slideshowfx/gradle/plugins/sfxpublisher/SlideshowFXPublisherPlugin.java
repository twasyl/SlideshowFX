package com.twasyl.slideshowfx.gradle.plugins.sfxpublisher;

import com.twasyl.slideshowfx.gradle.plugins.sfxpublisher.tasks.ListSnapshots;
import com.twasyl.slideshowfx.gradle.plugins.sfxpublisher.tasks.Publish;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin for publishing artifacts.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPublisherPlugin implements Plugin<Project> {
    public static final String PUBLISH_TASK_NAME = "publish";
    public static final String LIST_SNAPSHOTS_TASK_NAME = "listSnapshotProjects";

    @Override
    public void apply(Project project) {
        if (!project.equals(project.getRootProject())) {
            project.getTasks().create(PUBLISH_TASK_NAME, Publish.class);
        }

        project.getTasks().create(LIST_SNAPSHOTS_TASK_NAME, ListSnapshots.class);
    }
}
