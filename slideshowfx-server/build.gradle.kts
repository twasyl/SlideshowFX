plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module representing the server used within SlideshowFX"
version = "1.2-SNAPSHOT"

dependencies {
    api(group = "io.vertx", name = "vertx-core", version = project.property("dependencies.vertx.version") as String)
    api(group = "io.vertx", name = "vertx-web", version = project.property("dependencies.vertx.version") as String)

    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-utils"))
}

javafx {
    modules("javafx.base", "javafx.graphics", "javafx.web")
}