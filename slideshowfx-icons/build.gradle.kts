import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    id("java-library")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
}

description = "Module allowing to insert icons in the UI"
version = "1.1-SNAPSHOT"

dependencies {
    testImplementation(group = "org.testfx", name = "testfx-core", version = project.property("dependencies.testfx.version") as String)
    testImplementation(group = "org.testfx", name = "testfx-junit5", version = project.property("dependencies.testfx.version") as String)
    testImplementation(group = "org.testfx", name = "openjfx-monocle", version = project.property("dependencies.monocle.version") as String)
}

javafx {
    modules("javafx.graphics")
}

tasks.register("updateFontAwesome") {
    doLast {
        val fontawesomeVersion = project.findProperty("fontAwesomeVersion") as String ?: ""
        val fontaweomseDir = File("${buildDir}/tmp/fontawesome-update")
        val fontawesomeWebArchive = File("${fontaweomseDir}/fontawesome-free-${fontawesomeVersion}-web.zip")
        val fontawesomeWebDir = File("${fontaweomseDir}/fontawesome-free-${fontawesomeVersion}-web")
        val fontawesomeDesktopArchive = File("${fontaweomseDir}/fontawesome-free-${fontawesomeVersion}-desktop.zip")
        val fontawesomeDesktopDir = File("${fontaweomseDir}/fontawesome-free-${fontawesomeVersion}-desktop")

        if (!fontaweomseDir.exists()) {
            fontaweomseDir.mkdirs()
        }

        URL("https://use.fontawesome.com/releases/v${fontawesomeVersion}/fontawesome-free-${fontawesomeVersion}-web.zip")
                .openStream()
                .use { `in` -> Files.copy(`in`, fontawesomeWebArchive.toPath(), StandardCopyOption.REPLACE_EXISTING) }

        URL("https://use.fontawesome.com/releases/v${fontawesomeVersion}/fontawesome-free-${fontawesomeVersion}-desktop.zip")
                .openStream()
                .use { `in` -> Files.copy(`in`, fontawesomeDesktopArchive.toPath(), StandardCopyOption.REPLACE_EXISTING) }

        copy {
            from(zipTree(fontawesomeWebArchive))
            into(fontaweomseDir)
        }

        copy {
            from(zipTree(fontawesomeDesktopArchive))
            into(fontaweomseDir)
        }

        fontawesomeWebArchive.delete()
        fontawesomeDesktopArchive.delete()

        val versionDir = file("$projectDir/src/main/resources/com/twasyl/slideshowfx/icons/fontawesome/${fontawesomeVersion.replace('.', '_')}")
        versionDir.mkdir()

        val fontsDir = file("$versionDir/fonts")
        fontsDir.mkdir()

        val jsDir = file("$versionDir/js")
        jsDir.mkdir()

        copy {
            from(file("$fontawesomeWebDir/js/all.min.js"))
            into(jsDir)
        }

        copy {
            from(file("$fontawesomeDesktopDir/otfs"))
            include("*.otf")
            into(fontsDir)
            rename("Font Awesome 5 Brands-Regular-400.otf", "fontawesome-brand.otf")
            rename("Font Awesome 5 Free-Regular-400.otf", "fontawesome-regular.otf")
            rename("Font Awesome 5 Free-Solid-900.otf", "fontawesome-solid.otf")
        }

        fontaweomseDir.delete()
    }
}