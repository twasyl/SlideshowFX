plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "xtension allowing to insert images inside a SlideshowFX presentation"
version = "1.4-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-plugin-manager"))
    implementation(project(":slideshowfx-utils"))

    testImplementation(group = "org.mockito", name ="mockito-core", version = project.property("dependencies.mockito.version") as String)
    testImplementation(project(":slideshowfx-html"))
    testImplementation(project(":slideshowfx-markdown"))
    testImplementation(project(":slideshowfx-textile"))
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

sfxPlugin {
    contentExtension = true

    bundle {
        name = "Image"
        description = "Insert image in slides"
        setupWizardIconName = "PICTURE_ALT"
    }
}