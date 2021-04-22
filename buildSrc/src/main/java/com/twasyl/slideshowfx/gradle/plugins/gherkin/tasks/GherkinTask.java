package com.twasyl.slideshowfx.gradle.plugins.gherkin.tasks;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.tasks.testing.DefaultTestTaskReports;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.testing.TestTaskReports;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.util.ClosureBackedAction;

import java.io.File;

import static com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_SOURCE_SET_NAME;

/**
 * Tasks executing Gherkin scenarios. The task is currently using <a href="https://cucumber.io>Cucumber</a> as Gherkin
 * implementation.
 * <p>
 * The task also implements the {@link Reporting} interface to publish the reports. Both HTML and JUnit XML reports are
 * enabled by default.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class GherkinTask extends JavaExec implements Reporting<TestTaskReports> {

    private final TestTaskReports reports;

    public GherkinTask() {
        final SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        final SourceSet gherkinTests = sourceSets.getByName(GHERKIN_TEST_SOURCE_SET_NAME);

        setDescription("Run gherkin tests.");
        setGroup("verification");
        dependsOn("assemble", gherkinTests.getClassesTaskName());
        setEnabled(gherkinTests.getResources().getSrcDirs().stream().noneMatch(file -> !file.exists()));

        setMain("io.cucumber.core.cli.Main");
        configureClasspath();
        configureJvmArgs();
        adjustJvmArgsWhenJacocoEnabled();

        reports = getProject().getObjects().newInstance(DefaultTestTaskReports.class, this);
        configureReports();
    }

    private void configureClasspath() {
        final SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        final SourceSet main = sourceSets.getByName("main");
        final SourceSet gherkinTest = sourceSets.getByName(GHERKIN_TEST_SOURCE_SET_NAME);

        classpath(
                getProject().getConfigurations().getByName(GHERKIN_TEST_SOURCE_SET_NAME + "RuntimeClasspath"),
                main.getOutput(),
                main.getCompileClasspath(),
                gherkinTest.getOutput());
    }

    private void configureJvmArgs() {
        jvmArgs("--enable-preview", "-Dcucumber.publish.quiet=true", "-Dcucumber.publish.enabled=false");
    }

    private void adjustJvmArgsWhenJacocoEnabled() {
        getProject().getPlugins().withType(JacocoPlugin.class, jacocoPlugin -> {
            final var jacocoVersion = getProject().getExtensions().getByType(JacocoPluginExtension.class).getToolVersion();

            final var gherkinJacocoAgent = getProject().getConfigurations().create("gherkinJacocoAgent", configuration -> {
                configuration.withDependencies(dependencies -> {
                    final Dependency jacocoDependency = getProject().getDependencies().create("org.jacoco:org.jacoco.agent:" + jacocoVersion);
                    dependencies.add(jacocoDependency);
                });
            });

            final var jacocoAgent = getProject().zipTree(gherkinJacocoAgent.getSingleFile())
                    .filter(file -> file.getName().equals("jacocoagent.jar"))
                    .getSingleFile();
            final File jacocoResultDir = new File(getProject().getBuildDir(), "jacoco");
            final File jacocoResultFile = new File(jacocoResultDir, GHERKIN_TEST_SOURCE_SET_NAME + ".exec");

            jvmArgs("-javaagent:" + jacocoAgent.getAbsolutePath() + "=destfile=" + jacocoResultFile.getAbsolutePath());
        });
    }

    private void configureReports() {
        configureHtmlReport();
        configureJUnitReport();
    }

    private void configureHtmlReport() {
        this.reports.getHtml().setEnabled(true);
        final File reportsDir = new File(getProject().getBuildDir(), "reports");
        final File testsReportDir = new File(reportsDir, "tests");
        this.reports.getHtml().setDestination(new File(testsReportDir, GHERKIN_TEST_SOURCE_SET_NAME));
    }

    private void configureJUnitReport() {
        this.reports.getJunitXml().setEnabled(true);
        final File testResultsDir = new File(getProject().getBuildDir(), "test-results");
        this.reports.getJunitXml().setDestination(new File(testResultsDir, GHERKIN_TEST_SOURCE_SET_NAME));
    }

    @TaskAction
    @Override
    public void exec() {
        if (reports.getHtml().isEnabled()) {
            args("--plugin", String.format("html:%1$s", new File(reports.getHtml().getDestination().getAbsoluteFile(), "index.html")));
        }

        if (reports.getJunitXml().isEnabled()) {
            args("--plugin", String.format("junit:%1$s", new File(this.reports.getJunitXml().getDestination(), "TEST-gherkin.xml")));
        }

        args("--glue", "gradle.cucumber", getProject().getExtensions().getByType(SourceSetContainer.class).getByName(GHERKIN_TEST_SOURCE_SET_NAME).getResources().getSrcDirs().iterator().next());
        super.exec();
    }

    @Override
    @Nested
    public TestTaskReports getReports() {
        return reports;
    }

    @Override
    public TestTaskReports reports(Closure closure) {
        return reports(new ClosureBackedAction<>(closure));
    }

    @Override
    public TestTaskReports reports(Action<? super TestTaskReports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }
}
