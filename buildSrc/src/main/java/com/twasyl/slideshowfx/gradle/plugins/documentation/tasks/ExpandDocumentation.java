package com.twasyl.slideshowfx.gradle.plugins.documentation.tasks;

import com.twasyl.slideshowfx.gradle.plugins.DefaultSlideshowFXTask;
import com.twasyl.slideshowfx.gradle.plugins.documentation.extensions.DocumentationExtension;
import org.gradle.api.tasks.TaskAction;

import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.gradle.api.plugins.BasePlugin.BUILD_GROUP;

/**
 * Task expanding the documentation in order to replace variables with their values.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class ExpandDocumentation extends DefaultSlideshowFXTask<DocumentationExtension> {

    public ExpandDocumentation() {
        super(DocumentationExtension.class);
        this.setDescription("Expand the documentation by replacing variables with their values.");
        this.setGroup(BUILD_GROUP);

        this.getInputs().dir(this.extension.getDocsDir());
        this.getOutputs().dir(this.extension.getExpandDir());
    }

    @TaskAction
    protected void expand() {
        getProject().copy(copy -> copy.from(this.extension.getDocsDir())
                .into(this.extension.getExpandDir())
                .include("*.md")
                .expand(new HashMap<>(this.extension.getProperties().get()))
                .setFilteringCharset(UTF_8.displayName()));
    }
}
