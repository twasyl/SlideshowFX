package com.twasyl.slideshowfx.gradle.plugins.documentation;

import com.twasyl.slideshowfx.gradle.plugins.documentation.extensions.DocumentationExtension;
import com.twasyl.slideshowfx.gradle.plugins.documentation.tasks.ExpandDocumentation;
import com.twasyl.slideshowfx.gradle.plugins.documentation.tasks.RenderDocumentation;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * <p>Gradle plugin for rendering Markdown documentation to HTML.</p>
 * <h1>Tasks</h1>
 * <p>The plugin adds several tasks to the build:</p>
 * <ul>
 * <li><strong>{@value #EXPAND_DOCUMENTATION_TASK_NAME}</strong> implemented by the {@link com.twasyl.slideshowfx.gradle.plugins.documentation.tasks.ExpandDocumentation} task.</li>
 * <li><strong>{@value #RENDER_DOCUMENTATION_TASK_NAME}</strong> implemented by the {@link com.twasyl.slideshowfx.gradle.plugins.documentation.tasks.RenderDocumentation} task.</li>
 * </ul>
 *
 * <h1>Extension</h1>
 * <p>The plugin provides the {@value #DOCUMENTATION_EXTENSION_NAME} extension allowing to customize the default
 * behavior of the plugin</p>
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class Documentation implements Plugin<Project> {

    public static final String DOCUMENTATION_EXTENSION_NAME = "documentation";
    public static final String EXPAND_DOCUMENTATION_TASK_NAME = "expandDocumentation";
    public static final String RENDER_DOCUMENTATION_TASK_NAME = "renderDocumentation";

    @Override
    public void apply(Project project) {
        project.getExtensions().create(DOCUMENTATION_EXTENSION_NAME, DocumentationExtension.class, project);

        final ExpandDocumentation expandDocumentation = project.getTasks().create(EXPAND_DOCUMENTATION_TASK_NAME, ExpandDocumentation.class);
        project.getTasks().create(RENDER_DOCUMENTATION_TASK_NAME, RenderDocumentation.class).dependsOn(expandDocumentation);
    }
}
