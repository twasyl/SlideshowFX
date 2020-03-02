plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Snippet executor allowing to execute some Javascript inside a SlideshowFX presentation"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-plugin"))
}

javafx {
    modules("javafx.graphics")
}

sfxPlugin {
    snippetExecutor = true

    bundle {
        name = "JavaScript executor"
        description = "Execute JavaScript code inside a presentation"
    }
}