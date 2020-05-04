plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to insert links inside a SlideshowFX presentation"
version = "1.3-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-icons"))

    testImplementation(project(":slideshowfx-html"))
    testImplementation(project(":slideshowfx-markdown"))
    testImplementation(project(":slideshowfx-textile"))
    testImplementation(group = "org.mockito", name ="mockito-core", version = project.property("dependencies.mockito.version") as String)
}

javafx {
    modules("javafx.fxml", "javafx.graphics")
}

sfxPlugin {
    contentExtension = true

    bundle {
        name = "Link"
        description = "Insert links in slides"
        setupWizardIconName = "LINK"
    }
}