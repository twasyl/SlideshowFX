plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining how a content extension plugin should be developed"
version = "1.1"

dependencies {
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-markup"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-utils"))
}

javafx {
    modules("javafx.fxml", "javafx.graphics")
}