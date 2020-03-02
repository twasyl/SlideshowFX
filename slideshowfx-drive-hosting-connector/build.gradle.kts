plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Hosting connector allowing to open and save presentations from and to Google Drive"
version = "1.4-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-plugin"))

    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-hosting-connector"))

    pluginDependencies(group = "com.google.apis", name = "google-api-services-drive", version = project.property("dependencies.drive.version") as String)
    pluginDependencies(group = "com.google.auth", name = "google-auth-library-oauth2-http", version = "0.19.0")
}

javafx {
    modules("javafx.controls", "javafx.graphics", "javafx.web")
}

sfxPlugin {
    hostingConnector = true

    bundle {
        name = "Google Drive"
        description = "Support for connecting to Google Drive"
    }
}