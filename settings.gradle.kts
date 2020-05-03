pluginManagement {
    plugins {
        id("org.asciidoctor.jvm.convert") version "3.1.0"
        id("org.sonarqube") version "2.8"
        id("org.openjfx.javafxplugin") version "0.0.8"
    }
}

buildCache {
    local {
        directory = File(File(rootDir, ".gradle"), "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

buildscript {
    repositories {
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
        jcenter()
    }
    dependencies {
        classpath(group = "org.openjfx", name = "javafx-plugin", version = "0.0.8")
        classpath("org.asciidoctor:asciidoctor-gradle-jvm:3.1.0")
    }
}

rootProject.name = "slideshowfx"

include("slideshowfx-app")
include("slideshowfx-content-extension")
include("slideshowfx-documentation")
include("slideshowfx-engines")
include("slideshowfx-global-configuration")
include("slideshowfx-hosting-connector")
include("slideshowfx-icons")
include("slideshowfx-logs")
include("slideshowfx-markup")
include("slideshowfx-plugin")
include("slideshowfx-plugin-manager")
include("slideshowfx-server")
include("slideshowfx-setup")
include("slideshowfx-snippet-executor")
include("slideshowfx-style")
include("slideshowfx-ui-controls")
include("slideshowfx-utils")

// Content extension plugin
include("slideshowfx-alert-extension")
include("slideshowfx-code-extension")
include("slideshowfx-image-extension")
include("slideshowfx-link-extension")
include("slideshowfx-quiz-extension")
include("slideshowfx-quote-extension")
include("slideshowfx-sequence-diagram-extension")
include("slideshowfx-shape-extension")
include("slideshowfx-snippet-extension")

// Hosting connector plugins
include("slideshowfx-box-hosting-connector")
include("slideshowfx-drive-hosting-connector")
include("slideshowfx-dropbox-hosting-connector")

// Markup plugins
include("slideshowfx-asciidoctor")
include("slideshowfx-html")
include("slideshowfx-markdown")
include("slideshowfx-textile")

// Snippet executor plugins
include("slideshowfx-go-executor")
include("slideshowfx-golo-executor")
include("slideshowfx-groovy-executor")
include("slideshowfx-java-executor")
include("slideshowfx-javascript-executor")
include("slideshowfx-kotlin-executor")
include("slideshowfx-scala-executor")
include("slideshowfx-ruby-executor")