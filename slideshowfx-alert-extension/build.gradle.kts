import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to insert alerts inside a SlideshowFX presentation"
version = "1.3-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-icons"))

    testImplementation(group = "org.mockito", name = "mockito-core", version = project.property("dependencies.mockito.version") as String)

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
        name = "Alert"
        description = "Insert alert in slides"
        setupWizardIconName = "EXCLAMATION_TRIANGLE"
    }
}

tasks.register<Zip>("zipSweetAlertPackage") {
    archiveFileName.set("sweetalert2.zip")
    destinationDirectory.set(file("${buildDir}/tmp"))
    from("${buildDir}/tmp/sweetalert-update/sweetalert2") {
        into("sweetalert2")
    }
}

tasks.register("createSweetAlertPackage") {
    finalizedBy("zipSweetAlertPackage")

    doLast {
        val sweetAlertVersion = project.findProperty("sweetAlertVersion") as String ?: ""
        val binaryDir = File("${buildDir}/tmp/sweetalert-update")
        val newPackageDir = File("${binaryDir}/sweetalert2", sweetAlertVersion)
        val binary = File(newPackageDir, "sweetalert2.all.min.js")

        if (!newPackageDir.exists()) {
            newPackageDir.mkdirs()
        }

        URL("https://cdn.jsdelivr.net/npm/sweetalert2@${sweetAlertVersion}/dist/sweetalert2.all.min.js")
                .openStream()
                .use { `in` -> Files.copy(`in`, binary.toPath(), REPLACE_EXISTING) }
    }
}