plugins {
    id("sfx-plugin")
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-publisher")
}

description = "Extension allowing to define slide's content using the markdown syntax"
version = "1.2-SNAPSHOT"

dependencies {
    implementation(project(":slideshowfx-plugin"))

    pluginDependencies(group = "org.asciidoctor", name = "asciidoctorj", version = project.property("dependencies.asciidoctorj.version") as String)
    constraints {
        pluginDependencies("com.github.jnr:jnr-enxio:0.22") {
            because("previous versions is JPMS incompatible")
        }
        pluginDependencies("com.github.jnr:jnr-unixsocket:0.23") {
            because("previous versions is JPMS incompatible")
        }
    }
}

javafx {
    modules("javafx.graphics")
}

sfxPlugin {
    markupPlugin = true

    bundle {
        name = "Asciidoctor"
        description = "Define slide's content in Asciidoctor"
    }
}