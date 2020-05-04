import com.twasyl.slideshowfx.gradle.Utils.isMac
import com.twasyl.slideshowfx.gradle.Utils.isWindows
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.SlideshowFXPackager.CREATE_PACKAGE_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.CreatePackage
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.SFX_PLUGIN_EXTENSION
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import java.io.File.separator

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.asciidoctor:asciidoctor-gradle-jvm:3.1.0")
    }
}

plugins {
    java
    id("org.openjfx.javafxplugin")
    distribution
    jacoco
    id("org.sonarqube")
    id("sfx-packager")
}

description = "Module for the installer of SlideshowFX"
version = project.findProperty("productVersion") ?: System.getenv("PRODUCT_VERSION") ?: "@@NEXT-VERSION@@"

dependencies {
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-logs"))
    implementation(project(":slideshowfx-plugin-manager"))
    implementation(project(":slideshowfx-ui-controls"))
    implementation(project(":slideshowfx-utils"))
}

rootProject.subprojects.forEach {
    if (it.name != project.name) {
       project.evaluationDependsOn(it.path)
    }
}

packaging {
    val createPackage = project(":slideshowfx-app").tasks.getByName<CreatePackage>(CREATE_PACKAGE_TASK_NAME)
    val resourcesLocation = "\$ROOTDIR$separator" +
            when {
                isMac() -> "app"
                isWindows() -> "app"
                else -> "lib${separator}app"
            }
    outputDir = file("$buildDir/package")
    executableBaseName = "SlideshowFXSetup"

    runtime.modules = listOf("java.desktop", "java.logging", "java.scripting", "java.xml", "jdk.unsupported", "jdk.unsupported.desktop")

    app.jvmOpts = listOf("-Xms512m",
            "-Xmx1g",
            "--enable-preview",
            "-Dfile.encoding=UTF-8",
            "-Duser.language=en",
            "--add-modules", "ALL-MODULE-PATH",
            "-Dsetup.application.name=SlideshowFX",
            "-Dsetup.application.version=${project.version}",
            "-Dsetup.application.artifact=\"$resourcesLocation${separator}${createPackage.`package`.name}\"",
            "-Dsetup.plugins.directory=\"$resourcesLocation${separator}plugins\"",
            "-Dsetup.documentations.directory=\"$resourcesLocation${separator}documentations\"",
            "-Dsetup.service.twitter.consumerKey=${System.getenv("TWITTER_CONSUMER_KEY")}",
            "-Dsetup.service.twitter.consumerSecret=${System.getenv("TWITTER_CONSUMER_SECRET")}")
    
    app.module = "slideshowfx.setup/com.twasyl.slideshowfx.setup.app.SlideshowFXSetup"

    addResource(createPackage.outputs.files, createPackage.`package`.name)

    rootProject.subprojects.filter { it.pluginManager.hasPlugin("sfx-plugin") }
            .forEach { project ->
                val ext = project.extensions.get(SFX_PLUGIN_EXTENSION) as SlideshowFXPluginExtension
                val destination = ext.bundlePackageDestination
                if (destination != null) {
                    val bundle = project.tasks.getByName(BUNDLE_TASK_NAME)
                    addResource(bundle.outputs.files, destination)
                }
            }

    val asciidoctorTask = project(":slideshowfx-documentation").tasks.named<AsciidoctorTask>("asciidoctor").get()
    asciidoctorTask.backendOutputDirectories.forEach { backend ->
        addResource(fileTree(backend), "documentations")
    }
}

runApplication {
    module = "slideshowfx.setup"
    mainClass = "com.twasyl.slideshowfx.setup.app.SlideshowFXSetup"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}

distributions {
    main {
        contents {
            from(tasks.getByName<CreatePackage>(CREATE_PACKAGE_TASK_NAME).`package`)
        }
    }
}

tasks {
    jar {
        manifest {
            attributes(
                    "Implementation-Title" to "SlideshowFX-setup",
                    "Implementation-Version" to "${project.version}",
                    "Main-Class" to "com.twasyl.slideshowfx.setup.app.SlideshowFXSetup",
                    "JavaFX-Application-Class" to "com.twasyl.slideshowfx.setup.app.SlideshowFXSetup",
                    "JavaFX-Version" to "14+"
            )
        }
    }

    named<CreatePackage>(CREATE_PACKAGE_TASK_NAME) {
        dependsOn(
            ":slideshowfx-app:${CREATE_PACKAGE_TASK_NAME}",
            ":slideshowfx-documentation:asciidoctor",
            // Content extensions
            ":slideshowfx-alert-extension:bundle",
            ":slideshowfx-code-extension:bundle",
            ":slideshowfx-image-extension:bundle",
            ":slideshowfx-link-extension:bundle",
            ":slideshowfx-quiz-extension:bundle",
            ":slideshowfx-quote-extension:bundle",
            ":slideshowfx-sequence-diagram-extension:bundle",
            ":slideshowfx-shape-extension:bundle",
            ":slideshowfx-snippet-extension:bundle",
            // Hosting connectors
            ":slideshowfx-box-hosting-connector:bundle",
            ":slideshowfx-drive-hosting-connector:bundle",
            ":slideshowfx-dropbox-hosting-connector:bundle",
            // Snippet executors
            ":slideshowfx-go-executor:bundle",
            ":slideshowfx-golo-executor:bundle",
            ":slideshowfx-groovy-executor:bundle",
            ":slideshowfx-java-executor:bundle",
            ":slideshowfx-javascript-executor:bundle",
            ":slideshowfx-kotlin-executor:bundle",
            ":slideshowfx-ruby-executor:bundle",
            ":slideshowfx-scala-executor:bundle",
            // Markups
            ":slideshowfx-asciidoctor:bundle",
            ":slideshowfx-html:bundle",
            ":slideshowfx-markdown:bundle",
            ":slideshowfx-textile:bundle")
    }

    named<Zip>("distZip") {
        dependsOn(CREATE_PACKAGE_TASK_NAME)
        archiveFileName.set("${createPackage.get().distributionBaseName()}.zip")
    }

    named("distTar") {
        enabled = false
    }
}