plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Hosting connector allowing to open and save presentations from and to Box"
version = "1.3-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-plugin"))

    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))

    pluginDependencies(group = "com.box", name = "box-java-sdk", version = project.property("dependencies.box.version") as String)
}

javafx {
    modules("javafx.controls", "javafx.graphics", "javafx.web")
}

sfxPlugin {
    hostingConnector = true

    bundle {
        name = "Box"
        description = "Support for connecting to Box"
    }
}