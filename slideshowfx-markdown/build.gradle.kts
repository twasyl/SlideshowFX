plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to define slide's content using the markdown syntax"
version = "1.1-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-plugin"))

    pluginDependencies(group = "com.github.rjeschke", name = "txtmark", version = project.property("dependencies.markdown.version") as String)
}

javafx {
    modules("javafx.graphics")
}

sfxPlugin {
    markupPlugin = true

    bundle {
        name = "markdown"
        description = "Define slide's content in markdown"
    }
}