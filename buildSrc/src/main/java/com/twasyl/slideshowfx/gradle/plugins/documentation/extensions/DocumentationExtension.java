package com.twasyl.slideshowfx.gradle.plugins.documentation.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DocumentationExtension {
    private Project project;
    private MapProperty<String, Object> properties;
    private DirectoryProperty docsDir;
    private DirectoryProperty cssDir;
    private DirectoryProperty jsDir;
    private DirectoryProperty imagesDir;
    private DirectoryProperty fontsDir;
    private DirectoryProperty expandDir;
    private DirectoryProperty renderDir;

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

    public void properties(Map<String, Object> props) {
        this.properties.putAll(props);
    }

    public Map<String, ?> getProperties() {
        return new HashMap<>(this.properties.get());
    }

    public void expandDir(String dir) {
        this.expandDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getExpandDir() {
        return expandDir;
    }

    public void renderDir(String dir) {
        this.renderDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getRenderDir() {
        return renderDir;
    }

    public void docsDir(String dir) {
        this.docsDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getDocsDir() {
        return docsDir;
    }

    public void cssDir(String dir) {
        this.cssDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getCssDir() {
        return cssDir;
    }

    public void jsDir(String dir) {
        this.jsDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getJsDir() {
        return jsDir;
    }

    public void imagesDir(String dir) {
        this.imagesDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getImagesDir() {
        return imagesDir;
    }

    public void fontsDir(String dir) {
        this.fontsDir.set(this.project.getLayout().getProjectDirectory().dir(dir));
    }

    public DirectoryProperty getFontsDir() {
        return this.fontsDir;
    }
}