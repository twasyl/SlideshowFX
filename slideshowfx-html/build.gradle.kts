plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to define slide's content using the HTML syntax"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-plugin"))
}

javafx {
    modules("javafx.graphics")
}

sfxPlugin {
    markupPlugin = true

    bundle {
        name = "HTML support"
        description = "Define slide's content in HTML"
    }
}