package com.twasyl.slideshowfx.gradle.plugins.sfxpackager.extensions;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension for defining the packaging of the application.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PackageExtension {
    public static class Runtime {
        public List<String> modules;
    }

    public static class Application {
        public List<String> jvmOpts = new ArrayList<>();
        public String module;
    }

    public Project project;
    public File outputDir;
    public String executableBaseName;
    public Runtime runtime = new Runtime();
    public Application app = new Application();
    public Map<FileCollection, String> resources = new HashMap<>();

    @Inject
    public PackageExtension(Project project) {
        this.project = project;
    }

    public PackageExtension addResource(FileCollection from, String into) {
        this.resources.put(from, into);
        return this;
    }
}
