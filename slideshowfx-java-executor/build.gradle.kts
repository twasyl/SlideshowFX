plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Snippet executor allowing to execute some Java inside a SlideshowFX presentation"
version = "1.1-SNAPSHOT"

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
        name = "Java executor"
        description = "Execute Java code inside a presentation"
    }
}