plugins {
    `java-library`
    id("gherkin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module defining engines used by SlideshowFX"
version = "1.2"

dependencies {
    implementation(project(":slideshowfx-content-extension"))
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-utils"))
    implementation(group = "org.freemarker", name = "freemarker", version = project.property("dependencies.freemarker.version") as String)

    testImplementation(group = "org.mockito", name = "mockito-core", version = project.property("dependencies.mockito.version") as String)

    gherkinTestImplementation(group = "io.vertx", name = "vertx-core", version = project.property("dependencies.vertx.version") as String)
    gherkinTestImplementation(group = "io.vertx", name = "vertx-web", version = project.property("dependencies.vertx.version") as String)
}

javafx {
    modules("javafx.graphics", "javafx.swing")
}