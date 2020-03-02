plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining utilities to be used in the SlideshowFX application"
version = "1.2"

dependencies {
    api(group = "org.freemarker", name = "freemarker", version = project.property("dependencies.freemarker.version") as String)
    api(group = "org.jsoup", name = "jsoup", version = project.property("dependencies.jsoup.version") as String)
    api(group = "io.vertx", name = "vertx-core", version = project.property("dependencies.vertx.version") as String)

    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-style"))
}

javafx {
    modules("javafx.controls", "javafx.graphics")
}