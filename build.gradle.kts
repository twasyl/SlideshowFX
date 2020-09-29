import com.twasyl.slideshowfx.gradle.Utils.isMac
import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin
import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_SOURCE_SET_NAME
import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin
import org.gradle.api.file.DuplicatesStrategy.EXCLUDE
import org.openjfx.gradle.JavaFXOptions
import org.openjfx.gradle.JavaFXPlugin
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin
import org.sonarqube.gradle.SonarQubeTask
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.text.RegexOption.MULTILINE

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    id("org.sonarqube")
    `build-dashboard`
}

allprojects {
    repositories {
        jcenter()
    }
}

val integrationTest = "integrationTest"

subprojects {
    val moduleName by extra(project.name.replace("-", "."))
    val useBuildJdk = hasProperty("build_jdk")

    plugins.apply(IdeaPlugin::class)

    plugins.withType<IdeaPlugin>().configureEach {
        val idea = this
        this.model.module {
            outputDir = file("out${File.separator}production${File.separator}${moduleName}")
            testOutputDir = file("out${File.separator}test${File.separator}${moduleName}")
        }

        task<Delete>("ideaCleanOutput") {
            enabled = idea.model.module.outputDir.exists() || idea.model.module.testOutputDir.exists()
            delete = setOf(idea.model.module.outputDir, idea.model.module.testOutputDir)
        }
    }

    plugins.withType<JavaPlugin>().configureEach {
        val javaHome = property("build_jdk") as String
        val javaExecutablesPath = File(javaHome, "bin")

        fun javaExecutable(execName: String): String {
            val executableExtension = if (System.getProperty("os.name").toLowerCase().contains("windows")) ".exe" else ""
            val executable = File(javaExecutablesPath, "${execName}${executableExtension}")

            require(executable.exists()) { "There is no ${execName}${executableExtension} executable in $javaExecutablesPath" }
            return executable.toString()
        }

        fun jvmArgsForTests(): List<String> {
            val jvmArgs = mutableListOf("--enable-preview", "-Djava.awt.headless=true", "-Dtestfx.headless=true",
                    "-Dprism.order=sw", "-Dtestfx.robot=glass")

            if (isMac()) {
                jvmArgs.add("-Dprism.verbose=true")
            } else {
                jvmArgs.add("-Dprism.text=t2k")
            }

            return jvmArgs
        }

        val sourceSetContainer = project.extensions.getByType<SourceSetContainer>()
        val mainSourceSet = sourceSetContainer["main"]
        mainSourceSet.output.resourcesDir = mainSourceSet.output.classesDirs.elementAt(0)

        sourceSetContainer.create(integrationTest) {
            compileClasspath += mainSourceSet.output
            runtimeClasspath += mainSourceSet.output
        }

        configurations["${integrationTest}Implementation"].extendsFrom(configurations["implementation"])
        configurations["${integrationTest}RuntimeOnly"].extendsFrom(configurations["runtimeOnly"])

        dependencies {
            "testImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = project.property("dependencies.junit.version") as String)
            "testImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-params", version = project.property("dependencies.junit.version") as String)
            "testRuntimeOnly"(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = project.property("dependencies.junit.version") as String)

            "${integrationTest}Implementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = project.property("dependencies.junit.version") as String)
            "${integrationTest}Implementation"(group = "org.junit.jupiter", name = "junit-jupiter-params", version = project.property("dependencies.junit.version") as String)
            "${integrationTest}RuntimeOnly"(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = project.property("dependencies.junit.version") as String)
        }

        tasks {
            withType<JavaCompile>().configureEach {
                inputs.property("moduleName", moduleName)
                sourceCompatibility = JavaVersion.VERSION_15.toString()
                targetCompatibility = JavaVersion.VERSION_15.toString()
                modularity.inferModulePath.set(true)

                options.apply {
                    if (useBuildJdk) {
                        isFork = true
                        forkOptions.javaHome = file(javaHome)
                    }

                    compilerArgs.add("--enable-preview")
                }
            }

            named<JavaCompile>("compileJava").configure {
                options.apply {
                    if (useBuildJdk) {
                        sourcepath = files(mainSourceSet.java.srcDirs, mainSourceSet.resources.srcDirs)
                    }
                }
            }

            withType<Test>().configureEach {
                inputs.property("moduleName", moduleName)
                useJUnitPlatform()
                ignoreFailures = true

                if (useBuildJdk) {
                    this.executable = javaExecutable("java")
                }

                doFirst {
                    jvmArgs("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED",
                            "--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED")
                    jvmArgs(jvmArgsForTests())
                }
            }

            withType<Jar>().configureEach {
                inputs.property("moduleName", moduleName)
                duplicatesStrategy = EXCLUDE
                manifest {
                    attributes(mapOf("Implementation-Vendor" to "Thierry Wasylczenko"))
                }
            }

            withType<Javadoc>().configureEach {
                this.executable = javaExecutable("javadoc")
            }

            withType<JavaExec>().configureEach {
                this.executable = javaExecutable("java")
            }

            register<Test>(integrationTest).configure {
                dependsOn("jar")
                description = "Runs integration tests."
                group = "verification"
                useJUnitPlatform()
                testClassesDirs = sourceSetContainer[integrationTest].output.classesDirs
                classpath = sourceSetContainer[integrationTest].runtimeClasspath

                doFirst {
                    options {
                        jvmArgs(jvmArgsForTests())
                    }
                }
            }

            named("check").configure {
                dependsOn(integrationTest)
            }
        }
    }

    plugins.withType<JavaTestFixturesPlugin>().configureEach {
        dependencies {
            "testFixturesImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = project.property("dependencies.junit.version") as String)
            "testFixturesImplementation"(group = "org.junit.jupiter", name = "junit-jupiter-params", version = project.property("dependencies.junit.version") as String)
            "testFixturesRuntimeOnly"(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = project.property("dependencies.junit.version") as String)
        }
    }

    plugins.withType<GherkinPlugin>().configureEach {
        dependencies {
            "${GHERKIN_TEST_SOURCE_SET_NAME}Implementation"(group = "org.junit.jupiter", name = "junit-jupiter-api", version = project.property("dependencies.junit.version") as String)
            "${GHERKIN_TEST_SOURCE_SET_NAME}Implementation"(group = "org.junit.jupiter", name = "junit-jupiter-params", version = project.property("dependencies.junit.version") as String)
            "${GHERKIN_TEST_SOURCE_SET_NAME}Implementation"(group = "io.cucumber", name = "cucumber-java", version = project.property("dependencies.cucumber.version") as String)
        }

        tasks.named("check").configure {
            dependsOn(GHERKIN_TEST_TASK_NAME)
        }
    }

    plugins.withType<SlideshowFXPlugin>().configureEach {
        dependencies {
            "${integrationTest}Implementation"(project(":slideshowfx-plugin-manager"))
            "${integrationTest}Implementation"(testFixtures(project(":slideshowfx-plugin")))
        }

        tasks.getByName(integrationTest).dependsOn("bundle")
    }

    plugins.withType<JacocoPlugin>().configureEach {
        extensions.getByType<JacocoPluginExtension>().apply {
            toolVersion = "0.8.6"
        }

        tasks {
            withType<JacocoReport>().configureEach {
                executionData.setFrom(fileTree("$buildDir/jacoco").include("*.exec"))

                reports.apply {
                    csv.isEnabled = false
                    html.isEnabled = true
                    xml.isEnabled = true
                }

                dependsOn(integrationTest)
                if (plugins.hasPlugin(GherkinPlugin::class)) {
                    dependsOn(GHERKIN_TEST_TASK_NAME)
                }
            }

            named("check").configure {
                project.tasks.withType<JacocoReport>().forEach {
                    this.dependsOn(it)
                }
            }
        }
    }

    plugins.withType<JavaFXPlugin>().configureEach {
        extensions.getByType<JavaFXOptions>().apply {
            version = "15"
        }
    }

    plugins.withType<SonarQubePlugin>().configureEach {
        val sourceSetContainer = project.extensions.getByType<SourceSetContainer>()
        val requiredSourceSets = mutableListOf<SourceSet>(sourceSetContainer["test"], sourceSetContainer[integrationTest])

        if (plugins.hasPlugin(GherkinPlugin::class)) {
            requiredSourceSets.add(sourceSetContainer[GHERKIN_TEST_SOURCE_SET_NAME])
        }

        val testDirs = mutableListOf<File>()
        val jacocoReports = mutableListOf<File>()
        val junitReports = mutableListOf<File>()

        for (set in requiredSourceSets) {
            testDirs.addAll(set.allSource.srcDirs.filter { it.exists() })

            var resultFile = file("$buildDir/reports/jacoco/${set.name}/jacocoTestReport.xml")
            if (resultFile.exists()) {
                jacocoReports.add(resultFile)
            }

            resultFile = file("$buildDir/reports/tests/${set.name}")
            if (resultFile.exists()) {
                junitReports.add(resultFile)
            }
        }

        extensions.getByType<SonarQubeExtension>().apply {
            properties {
                property("sonar.tests", testDirs.joinToString(separator = ",") { it.absolutePath })
                property("sonar.coverage.jacoco.xmlReportPaths", jacocoReports.joinToString(separator = ",") { it.absolutePath })
                property("sonar.junit.reportPaths", junitReports.joinToString(separator = ",") { it.absolutePath })
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.host.url", System.getProperty("sonar.host.url", System.getenv("SONAR_HOST_URL")))
        property("sonar.login", System.getProperty("sonar.login", System.getenv("SONAR_LOGIN")))
        property("sonar.projectKey", System.getProperty("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY")))
        property("sonar.organization", System.getProperty("sonar.organization", System.getenv("SONAR_ORGANIZATION")))
        property("sonar.branch.name", System.getProperty("sonar.branch.name", System.getenv("SONAR_BRANCH")))
        property("sonar.sourceEncoding", "UTF-8")
    }
}

tasks {
    withType<SonarQubeTask>().configureEach {
        dependsOn(subprojects.filter { project -> project.pluginManager.hasPlugin("org.sonarqube") }
                .map { project -> project.tasks.named("check") })
    }

    register("updateProductVersionNumber") {
        group = "Release"
        description = "Update the product version number in all relevant files."

        doLast {
            val excludeBuildFolders = fun(it: File): Boolean {
                return it.name != "build" && it.name != ".gradle"
            }
            val filesToBeEventuallyUpdated = fun(it: File): Boolean {
                return it.extension == "java" || it.extension == "yml"
            }
            val updateContent = fun(it: File) {
                val nextVersionToken = "@@NEXT-VERSION@@"
                val newVersion = (project.findProperty("productVersion") ?: System.getenv("PRODUCT_VERSION")) as String
                val content = it.readText(charset = UTF_8)

                if (content.contains(nextVersionToken)) {
                    logger.info("File {} will be updated", rootDir.toPath().relativize(it.toPath()))
                    it.writeText(content.replace(nextVersionToken, newVersion), charset = UTF_8)
                }
            }

            val dirs = mutableListOf(File(rootDir, "buildSrc"), File(rootDir, ".github"))
            dirs.addAll(subprojects.map { it.projectDir })
            dirs.forEach {
                it.walkTopDown()
                        .onEnter(excludeBuildFolders)
                        .filter(filesToBeEventuallyUpdated)
                        .forEach(updateContent)
            }
        }
    }

    register("removeSnapshots") {
        group = "Release"
        description = "Remove the -SNAPSHOT qualifier from versions."

        doLast {
            val search = Regex("(^version\\s+=\\s+\"\\d+(?:\\.\\d+){0,1})-SNAPSHOT(\"$)", MULTILINE)
            val replacement = fun(result: MatchResult): String {
                return result.groups.elementAt(1)?.value + result.groups.elementAt(2)?.value
            }
            subprojects.filter { it.version.toString().endsWith("-SNAPSHOT") }
                    .map { it.buildFile }
                    .forEach {
                        logger.info("Updating build file {}", it)
                        val content = it.readText(charset = UTF_8)
                        it.writeText(content.replace(search, replacement), charset = UTF_8)
                    }
        }
    }
}