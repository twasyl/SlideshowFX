plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining a snippet executor"
version = "1.0"

dependencies {
    api(project(":slideshowfx-ui-controls"))
    api(project(":slideshowfx-utils"))
    api(project(":slideshowfx-plugin"))

    implementation(project(":slideshowfx-global-configuration"))
}

javafx {
    modules("javafx.fxml", "javafx.graphics")
}