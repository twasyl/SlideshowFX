package com.twasyl.slideshowfx.gradle.plugins.documentation.tasks;

import com.twasyl.slideshowfx.gradle.plugins.documentation.extensions.DocumentationExtension;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

/**
 * Task expanding the documentation in order to replace variables with their values.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ExpandDocumentation extends Copy {
    private DocumentationExtension extension;

    @Inject
    public ExpandDocumentation(final DocumentationExtension extension) {
        this.setDescription("Expand the documentation by replacing variables with their values.");
        this.setGroup("build");

        this.extension = extension;
        this.getInputs().dir(this.extension.getDocsDir());
        this.getOutputs().dir(this.extension.getExpandDir());

        from(this.extension.getDocsDir());
        into(this.extension.getExpandDir());
        include("*.md");
    }

    @Override
    @TaskAction
    protected void copy() {
        this.expand(this.extension.getProperties());
        this.into(this.extension.getExpandDir());
        super.copy();
    }
}
