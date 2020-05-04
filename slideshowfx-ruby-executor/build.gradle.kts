plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Snippet executor allowing to execute some Ruby inside a SlideshowFX presentation"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-utils"))
}

javafx {
    modules("javafx.controls", "javafx.graphics")
}

sfxPlugin {
    snippetExecutor = true

    bundle {
        name = "Ruby executor"
        description = "Execute Ruby code inside a presentation"
    }
}