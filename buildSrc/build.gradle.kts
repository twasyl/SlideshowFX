plugins {
    `java-library`
    `java-gradle-plugin`
}

repositories {
    jcenter()
}

dependencies {
    implementation(group = "org.jsoup", name = "jsoup", version = "1.13.1")
    implementation(group = "com.atlassian.commonmark", name = "commonmark", version = "0.15.1")
    implementation(group = "com.atlassian.commonmark", name = "commonmark-ext-gfm-tables", version = "0.15.1")
}

gradlePlugin {
    plugins {
        create("SlideshowFXPlugin") {
            id = "sfx-plugin"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin"
        }

        create("SlideshowFXPackager") {
            id = "sfx-packager"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager"
        }

        create("Gherkin") {
            id = "gherkin"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin"
        }

        create("Documentation") {
            id = "documentation"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.documentation.Documentation"
        }
    }
}

if (project.hasProperty("build_jdk")) {
    tasks {
        withType<JavaCompile>().configureEach {
            options.apply {
                isFork = true
                forkOptions.javaHome = file(project.property("build_jdk") as String)
            }
        }
    }
}