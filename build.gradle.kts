import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin
import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_SOURCE_SET_NAME
import com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin.GHERKIN_TEST_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin
import org.gradle.api.file.DuplicatesStrategy.EXCLUDE
import org.openjfx.gradle.JavaFXOptions
import org.openjfx.gradle.JavaFXPlugin
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin

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

    plugins.withType<JavaPlugin>().configureEach {
        val javaHome = property("build_jdk") as String
        val javaExecutablesPath = File(javaHome, "bin")

        fun javaExecutable(execName: String): String {
            val executableExtension = if (System.getProperty("os.name").toLowerCase().contains("windows")) ".exe" else ""
            val executable = File(javaExecutablesPath, "${execName}${executableExtension}")

            require(executable.exists()) { "There is no ${execName}${executableExtension} executable in $javaExecutablesPath" }
            return executable.toString()
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
                sourceCompatibility = JavaVersion.VERSION_HIGHER.toString()
                targetCompatibility = JavaVersion.VERSION_HIGHER.toString()

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

                    doFirst {
                        if (!classpath.isEmpty) {
                            compilerArgs.addAll(arrayOf("--module-path", classpath.asPath))
                            classpath = files()
                        }
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
                    jvmArgs("--enable-preview",
                            "--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED",
                            "--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED")
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

            named("check").configure {
                dependsOn(integrationTest)
            }
        }

        task<Test>(integrationTest) {
            dependsOn("jar")
            description = "Runs integration tests."
            group = "verification"

            useJUnitPlatform()
            testClassesDirs = sourceSetContainer[integrationTest].output.classesDirs
            classpath = sourceSetContainer[integrationTest].runtimeClasspath
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
            "${GHERKIN_TEST_SOURCE_SET_NAME}Implementation"(group = "io.cucumber", name = "cucumber-java", version = "5.4.1")
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
        tasks.withType<JacocoReport>().configureEach {

            val sourceSetContainer = project.extensions.getByType<SourceSetContainer>()
            sourceSets(sourceSetContainer[integrationTest])

            if (plugins.hasPlugin(GherkinPlugin::class)) {
                sourceSets(sourceSetContainer[GHERKIN_TEST_SOURCE_SET_NAME])
            }

            executionData(fileTree("$buildDir/jacoco").matching { include("*.exec") })

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
    }

    plugins.withType<JavaFXPlugin>().configureEach {
        extensions.getByType<JavaFXOptions>().apply {
            version = "14-ea+8"
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

            var resultFile = file("$buildDir/jacoco/${set.name}.exec")
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
                property("sonar.jacoco.reportPaths", jacocoReports.joinToString(separator = ",") { it.absolutePath })
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
    }
}

tasks.sonarqube {
    dependsOn(subprojects.filter { project -> project.pluginManager.hasPlugin("org.sonarqube") }
            .map { project -> project.tasks.named("check") })
}