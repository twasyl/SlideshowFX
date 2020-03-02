plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Extension allowing to define slide's content using the textile syntax"
version = "1.3-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-plugin"))

    pluginDependencies(group = "org.eclipse.mylyn.docs", name = "org.eclipse.mylyn.wikitext.textile", version = project.property("dependencies.wikitext.version") as String)
}

javafx {
    modules("javafx.graphics")
}

sfxPlugin {
    markupPlugin = true

    bundle {
        name = "Textile"
        description = "Define slide's content in Textile"
    }
}