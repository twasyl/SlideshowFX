package com.twasyl.slideshowfx.gradle.plugins.gherkin.tasks;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.tasks.testing.DefaultTestTaskReports;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.testing.TestTaskReports;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.util.ClosureBackedAction;

import java.io.File;

import static com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_SOURCE_SET_NAME;
import static java.io.File.separator;

/**
 * Tasks executing Gherkin scenarios. The task is currently using <a href="https://cucumber.io>Cucumber</a> as Gherkin
 * implementation.
 *
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
        setEnabled(gherkinTests.getResources().getSrcDirs().stream().filter(file -> !file.exists()).count() == 0);

        setMain("io.cucumber.core.cli.Main");
        configureClasspath();
        configureJvmArgs();

        reports = getProject().getObjects().newInstance(DefaultTestTaskReports.class, this);
        configureReports();
    }

    private void configureClasspath() {
        final SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        final SourceSet main = sourceSets.getByName("main");
        final SourceSet gherkinTest = sourceSets.getByName(GHERKIN_TEST_SOURCE_SET_NAME);

        classpath(
                getProject().getConfigurations().getByName(GHERKIN_TEST_SOURCE_SET_NAME + "Runtime"),
                main.getOutput(),
                main.getCompileClasspath(),
                gherkinTest.getOutput());
    }

    private void configureJvmArgs() {
        final Configuration jacocoAgentConfiguration = getProject().getConfigurations().findByName(JacocoPlugin.AGENT_CONFIGURATION_NAME);
        if (jacocoAgentConfiguration != null) {
            jvmArgs(String.format("-javaagent:%2$s=destfile=%3s%1$sjacoco%1$s%4s.exec", separator, jacocoAgentConfiguration.getSingleFile().getAbsolutePath(), getProject().getBuildDir().getAbsolutePath(), GHERKIN_TEST_SOURCE_SET_NAME));
        }

        jvmArgs("--enable-preview");
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
            args("--plugin", String.format("html:%1$s", reports.getHtml().getDestination().getAbsolutePath()));
        }

        if (reports.getJunitXml().isEnabled()) {
            args("--plugin", String.format("junit:%1$s", new File(this.reports.getJunitXml().getDestination(), "TEST-gherkin.xml")));
        }

        args("--strict", "--glue", "gradle.cucumber", getProject().getExtensions().getByType(SourceSetContainer.class).getByName(GHERKIN_TEST_SOURCE_SET_NAME).getResources().getSrcDirs().iterator().next());
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
