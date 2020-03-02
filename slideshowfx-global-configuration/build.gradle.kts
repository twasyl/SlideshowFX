plugins {
    id("java-library")
    jacoco
    id("org.sonarqube")
}

description = "Module defining the global configuration of SlideshowFX"
version = "1.1"

dependencies {
    implementation(project(":slideshowfx-logs"))

    testImplementation(group = "org.mockito", name ="mockito-core", version = project.property("dependencies.mockito.version") as String)
}