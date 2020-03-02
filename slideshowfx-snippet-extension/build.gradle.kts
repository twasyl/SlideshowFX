plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Extension allowing to insert executable code snippets inside a SlideshowFX presentation"
version = "1.3-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-plugin-manager"))
    implementation(project(":slideshowfx-snippet-executor"))

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
        name = "Executable snippet"
        description = "Insert executable code snippets in presentations"
        setupWizardIconName = "TERMINAL"
    }
}