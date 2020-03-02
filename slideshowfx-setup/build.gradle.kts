import com.twasyl.slideshowfx.gradle.plugins.sfxpackager.tasks.CreatePackage
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.BUNDLE_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin.SFX_PLUGIN_EXTENSION
import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions.SlideshowFXPluginExtension
import org.asciidoctor.gradle.jvm.AsciidoctorTask

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
    id("sfx-publisher")
}

description = "Module for the installer of SlideshowFX"
version = "@@NEXT_VERSION@@"

dependencies {
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-logs"))
    implementation(project(":slideshowfx-plugin-manager"))
    implementation(project(":slideshowfx-ui-controls"))
    implementation(project(":slideshowfx-utils"))
}

val createPackage = tasks.getByName<CreatePackage>("createPackage")

packaging {
    outputDir = file("${getBuildDir()}/package")
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
            "-Dsetup.application.artifact=./resources/${createPackage.`package`.name}",
            "-Dsetup.plugins.directory=./resources/plugins",
            "-Dsetup.documentations.directory=./resources/documentations",
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
    // TODO Make this computation work again
/*
    val asciidoctorTask = project(":slideshowfx-documentation").tasks.getByName("asciidoctor", AsciidoctorTask::class)
    asciidoctorTask.backendOutputDirectories.forEach { backend ->
        addResource(fileTree(backend), "documentations")
    }
 */
}

runApplication {
    module = "slideshowfx.setup"
    mainClass = "com.twasyl.slideshowfx.setup.app.SlideshowFXSetup"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}

tasks.processResources {
    doLast {
        // TODO Remove this task when using JVM properties is properly working
//        ant.propertyfile(file: "${sourceSets.main.output.resourcesDir}/com/twasyl/slideshowfx/setup/setup.properties") {
//        entry(key: "setup.application.name", value: "SlideshowFX")
//        entry(key: "setup.application.version", value: "${version}")
//        entry(key: "setup.application.artifact", value: "./resources/${project(":slideshowfx-app").createPackage.package.name}")
//        entry(key: "setup.plugins.directory", value: "./resources/plugins")
//        entry(key: "setup.documentations.directory", value: "./resources/documentations")
//        entry(key: "setup.service.twitter.consumerKey", value: "${System.env["TWITTER_CONSUMER_KEY"]}")
//        entry(key: "setup.service.twitter.consumerSecret", value: "${System.env["TWITTER_CONSUMER_SECRET"]}")
//    }
    }
}

tasks.jar {
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

distributions {
    main {
        contents {
            from(createPackage.`package`)
        }
    }
}

tasks.named("createPackage") {
    dependsOn(":slideshowfx-app:createPackage", ":slideshowfx-documentation:asciidoctor",
            ":slideshowfx-alert-extension:bundle",
            ":slideshowfx-box-hosting-connector:bundle",
            ":slideshowfx-code-extension:bundle",
            ":slideshowfx-drive-hosting-connector:bundle",
            ":slideshowfx-dropbox-hosting-connector:bundle",
            ":slideshowfx-go-executor:bundle",
            ":slideshowfx-golo-executor:bundle",
            ":slideshowfx-groovy-executor:bundle",
            ":slideshowfx-html:bundle",
            ":slideshowfx-image-extension:bundle",
            ":slideshowfx-java-executor:bundle",
            ":slideshowfx-javascript-executor:bundle",
            ":slideshowfx-kotlin-executor:bundle",
            ":slideshowfx-link-extension:bundle",
            ":slideshowfx-markdown:bundle",
            ":slideshowfx-quiz-extension:bundle",
            ":slideshowfx-quote-extension:bundle",
            ":slideshowfx-ruby-executor:bundle",
            ":slideshowfx-scala-executor:bundle",
            ":slideshowfx-sequence-diagram-extension:bundle",
            ":slideshowfx-shape-extension:bundle",
            ":slideshowfx-snippet-extension:bundle",
            ":slideshowfx-textile:bundle")
}

tasks.named<Zip>("distZip") {
    dependsOn("createPackage")
    archiveFileName.set("${createPackage.distributionBaseName()}.zip")
}

tasks.named("distTar") {
    enabled = false
}