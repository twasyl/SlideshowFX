plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module allowing to insert icons in the UI"
version = "1.1-SNAPSHOT"

dependencies {
    testImplementation(group = "org.testfx", name = "testfx-core", version = project.property("dependencies.testfx.version") as String)
    testImplementation(group = "org.testfx", name = "testfx-junit5", version = project.property("dependencies.testfx.version") as String)
    testImplementation(group = "org.testfx", name = "openjfx-monocle", version = project.property("dependencies.monocle.version") as String)
}

javafx {
    modules("javafx.graphics")
}