plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Snippet executor allowing to execute some Scala inside a SlideshowFX presentation"
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
        name = "Scala executor"
        description = "Execute Scala code inside a presentation"
    }
}