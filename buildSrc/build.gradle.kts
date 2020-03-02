plugins {
    `java-library`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("SlideshowFXPlugin") {
            id = "sfx-plugin"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin"
        }

        create("SlideshowFXPublisher") {
            id = "sfx-publisher"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.sfxpublisher.SlideshowFXPublisherPlugin"
        }

        create("SlideshowFXPackager") {
            id = "sfx-packager"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager"
        }

        create("Gherkin") {
            id = "gherkin"
            implementationClass = "com.twasyl.slideshowfx.gradle.plugins.gherkin.GherkinPlugin"
        }
    }
}

if (project.hasProperty("build_jdk")) {
    tasks.compileJava {
        doFirst {
            options.isFork = true
            options.forkOptions.javaHome = file(project.property("build_jdk") as String)
        }
    }
}