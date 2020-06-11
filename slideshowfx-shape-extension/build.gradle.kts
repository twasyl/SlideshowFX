import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Extension allowing to insert shapes inside a SlideshowFX presentation"
version = "1.1-SNAPSHOT"

dependencies {
    api(project(":slideshowfx-markup"))
    api(project(":slideshowfx-plugin"))
    api(project(":slideshowfx-ui-controls"))

    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-utils"))

    testImplementation(group = "org.mockito", name ="mockito-core", version = project.property("dependencies.mockito.version") as String)
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.graphics")
}

sfxPlugin {
    contentExtension = true

    bundle {
        name = "Shape"
        description = "Insert shapes in slides"
        setupWizardIconName = "STAR"
    }
}

tasks.register<Zip>("zipSnapSVGPackage") {
    archiveFileName.set("snapsvg.zip")
    destinationDirectory.set(file("${buildDir}/tmp"))
    from("${buildDir}/tmp/snapsvg-update/snapsvg") {
        into("snapsvg")
    }
}

tasks.register("createSnapSVGPackage") {
    finalizedBy("zipSnapSVGPackage")

    doLast {
        val snapSvgVersion = project.findProperty("snapSvgVersion") as String ?: ""
        val binaryDir = File("${buildDir}/tmp/snapsvg-update")
        val newPackageDir = File("${binaryDir}/snapsvg", snapSvgVersion)
        val binary = File("${binaryDir}/Snap.svg-${snapSvgVersion}.zip")
        val unpackedBinary = File(binaryDir, "Snap.svg-${snapSvgVersion}")

        if (!binaryDir.exists()) {
            binaryDir.mkdirs()
        }

        if (!newPackageDir.exists()) {
            newPackageDir.mkdirs()
        }

        URL("https://github.com/adobe-webplatform/Snap.svg/archive/v${snapSvgVersion}.zip")
                .openStream()
                .use { `in` -> Files.copy(`in`, binary.toPath(), REPLACE_EXISTING) }

        copy {
            from(zipTree(binary))
            into(binaryDir)
        }

        binary.delete()

        copy {
            from(fileTree("${unpackedBinary}/dist"))
            include("snap.svg-min.js")
            into(newPackageDir)
        }

        unpackedBinary.deleteRecursively()
    }
}