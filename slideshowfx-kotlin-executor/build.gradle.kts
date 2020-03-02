plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Snippet executor allowing to execute some Kotlin inside a SlideshowFX presentation"
version = "1.2-SNAPSHOT"

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
        name = "Kotlin executor"
        description = "Execute Kotlin code inside a presentation"
    }
}