plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining controls to be used within the SlideshowFX app"
version = "1.3"

dependencies {
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-plugin-manager"))
}

javafx {
    modules("javafx.controls", "javafx.graphics")
}