plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Hosting connector allowing to open and save presentations from and to Dropbox"
version = "1.3-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-plugin"))

    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))

    pluginDependencies(group = "com.dropbox.core", name = "dropbox-core-sdk", version = project.property("dependencies.dropbox.version") as String)
}

javafx {
    modules("javafx.controls", "javafx.graphics", "javafx.web")
}

sfxPlugin {
    hostingConnector = true

    bundle {
        name = "Dropbox"
        description = "Support for connecting to Dropbox"
    }
}