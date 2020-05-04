plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to insert quizs inside a SlideshowFX presentation"
version = "1.2-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-server"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-icons"))

    integrationTestImplementation(group = "org.testfx", name = "testfx-core", version = project.property("dependencies.testfx.version") as String)
    integrationTestImplementation(group = "org.testfx", name = "testfx-junit5", version = project.property("dependencies.testfx.version") as String)
    integrationTestImplementation(group = "org.testfx", name = "openjfx-monocle", version = project.property("dependencies.monocle.version") as String)
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

sfxPlugin {
    contentExtension = true

    bundle {
        name = "Quiz"
        description = "Insert quiz in slides"
        setupWizardIconName = "QUESTION"
    }
}