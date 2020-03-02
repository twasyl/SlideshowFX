plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining a plugin"
version = "1.0"

dependencies {
    implementation(project(":slideshowfx-plugin"))
}

javafx {
    modules("javafx.graphics")
}