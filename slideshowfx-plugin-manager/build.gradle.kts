plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining a plugin system for the application"
version = "1.2"

dependencies {
    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-utils"))
}

javafx {
    modules("javafx.graphics")
}