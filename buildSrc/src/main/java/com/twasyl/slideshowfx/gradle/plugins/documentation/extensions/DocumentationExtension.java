package com.twasyl.slideshowfx.gradle.plugins.documentation.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;

import javax.inject.Inject;

public class DocumentationExtension {
    private Project project;
    private final MapProperty<String, Object> properties;
    private final DirectoryProperty docsDir;
    private final DirectoryProperty cssDir;
    private final DirectoryProperty jsDir;
    private final DirectoryProperty imagesDir;
    private final DirectoryProperty fontsDir;
    private final DirectoryProperty expandDir;
    private final DirectoryProperty renderDir;

    @Inject
    public DocumentationExtension(Project project) {
        this.project = project;
        final var objects = this.project.getObjects();
        this.properties = objects.mapProperty(String.class, Object.class);

        this.docsDir = objects.directoryProperty();
        this.docsDir.convention(this.project.getLayout().getProjectDirectory().dir("src/main/docs"));

        this.cssDir = objects.directoryProperty();
        this.cssDir.convention(this.project.getLayout().getProjectDirectory().dir("src/main/resources/css"));

        this.jsDir = objects.directoryProperty();
        this.jsDir.convention(this.project.getLayout().getProjectDirectory().dir("src/main/resources/js"));

        this.imagesDir = objects.directoryProperty();
        this.imagesDir.convention(this.project.getLayout().getProjectDirectory().dir("src/main/resources/images"));

        this.fontsDir = objects.directoryProperty();
        this.fontsDir.convention(this.project.getLayout().getProjectDirectory().dir("src/main/resources/webfonts"));

        this.expandDir = objects.directoryProperty();
        this.expandDir.convention(this.project.getLayout().getBuildDirectory().dir("/tmp/documentation/markdown"));

        this.renderDir = objects.directoryProperty();
        this.renderDir.convention(this.project.getLayout().getProjectDirectory().dir(this.project.getBuildDir().getName() + "/docs"));
    }

    public MapProperty<String, Object> getProperties() {
        return properties;
    }

    public DirectoryProperty getDocsDir() {
        return docsDir;
    }

    public DirectoryProperty getCssDir() {
        return cssDir;
    }

    public DirectoryProperty getJsDir() {
        return jsDir;
    }

    public DirectoryProperty getImagesDir() {
        return imagesDir;
    }

    public DirectoryProperty getFontsDir() {
        return fontsDir;
    }

    public DirectoryProperty getExpandDir() {
        return expandDir;
    }

    public DirectoryProperty getRenderDir() {
        return renderDir;
    }
}