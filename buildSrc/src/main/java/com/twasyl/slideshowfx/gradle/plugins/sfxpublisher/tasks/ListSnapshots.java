package com.twasyl.slideshowfx.gradle.plugins.sfxpublisher.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ListSnapshots extends DefaultTask {

    @TaskAction
    public void list() {
        getProject().getRootProject().getSubprojects().forEach(project -> {
            System.out.println("Project " + project.getName() + " is in version " + project.getVersion());
        });
    }
}
