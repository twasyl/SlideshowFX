package com.twasyl.slideshowfx.gradle.plugins.gherkin;

import com.twasyl.slideshowfx.gradle.plugins.gherkin.tasks.GherkinTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * A plugin allowing to execute tests written using the Gherkin syntax. The plugin:
 * <ul>
 *     <li>creates a dedicated source set named {@link #GHERKIN_TEST_SOURCE_SET_NAME} into which the tests and scenarios
 *     must be placed;</li>
 *     <li>configures the created configurations (created by the source set creation);</li>
 *     <li>creates a task named {@link #GHERKIN_TEST_TASK_NAME} to execute the tests</li>
 * </ul>
 */
public class GherkinPlugin implements Plugin<Project> {
    public static final String GHERKIN_TEST_SOURCE_SET_NAME = "gherkinTest";
    public static final String GHERKIN_TEST_TASK_NAME = "gherkinTest";

    @Override
    public void apply(Project project) {
        createSourceSet(project);
        configureConfigurations(project);
        project.getTasks().create(GHERKIN_TEST_TASK_NAME, GherkinTask.class);
    }

    private void createSourceSet(final Project project) {
        final var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        final var main = sourceSets.getByName("main");

        sourceSets.create(GHERKIN_TEST_SOURCE_SET_NAME, gherkinTest -> {
            gherkinTest.setCompileClasspath(gherkinTest.getCompileClasspath().plus(main.getOutput()));
            gherkinTest.setRuntimeClasspath(gherkinTest.getRuntimeClasspath().plus(main.getOutput()));
        });
    }

    private void configureConfigurations(final Project project) {
        final var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        final var main = sourceSets.getByName("main");
        final var gherkinTest = sourceSets.getByName(GHERKIN_TEST_SOURCE_SET_NAME);
        final var configurations = project.getConfigurations();

        configurations.getByName(gherkinTest.getImplementationConfigurationName()).extendsFrom(configurations.getByName(main.getImplementationConfigurationName()));
        configurations.getByName(GHERKIN_TEST_SOURCE_SET_NAME + "RuntimeOnly").extendsFrom(configurations.getByName(gherkinTest.getImplementationConfigurationName()));
    }
}
