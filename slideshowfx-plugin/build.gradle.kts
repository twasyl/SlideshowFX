plugins {
    id("org.openjfx.javafxplugin")
    `java-test-fixtures`
    jacoco
    id("org.sonarqube")
}

description = "Module defining a markup"
version = "1.0"

dependencies {
    testFixturesImplementation(project(":slideshowfx-plugin-manager"))
}

javafx {
    modules("javafx.graphics")
}