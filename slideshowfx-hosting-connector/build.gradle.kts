plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining a hosting connector allowing to connect to remote cloud service"
version = "1.2-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-utils"))
}

javafx {
    modules("javafx.controls")
}