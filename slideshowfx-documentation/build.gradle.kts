import org.gradle.util.GradleVersion
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask

plugins {
    id("org.asciidoctor.jvm.convert")
}

description = "SlideshowFX documentation to be included within the application and the setup package"
version = "1.0"

tasks {
    "asciidoctor"(AbstractAsciidoctorTask::class) {
        attributes(
                mapOf(
                        "source-highlighter" to "coderay",
                        "toc" to "left",
                        "icons" to "font",
                        "iconfont-remote!" to "",
                        "webfonts!" to "",
                        "setanchors" to "",
                        "sectlinks" to "",
                        "slideshowfx_version" to rootProject.version,
                        "asciidoctor-source" to "${project.projectDir}/src/docs/asciidoc",
                        "jdk-version" to "14",
                        "javafx-version" to "14",
                        "gradle-version" to GradleVersion.current().version,
                        "asciidoctorj-version" to project.property("dependencies.asciidoctorj.version") as String,
                        "freemarker-version" to project.property("dependencies.freemarker.version") as String,
                        "jsoup-version" to project.property("dependencies.jsoup.version") as String,
                        "wikitext-textile-core-version" to project.property("dependencies.wikitext.version") as String,
                        "txtmark-version" to project.property("dependencies.markdown.version") as String,
                        "vertx-version" to project.property("dependencies.vertx.version") as String,
                        "zxing-jse-version" to project.property("dependencies.zxing.version") as String,
                        "box-version" to project.property("dependencies.box.version") as String,
                        "drive-version" to project.property("dependencies.drive.version") as String,
                        "dropbox-version" to project.property("dependencies.dropbox.version") as String,
                        "ace-version" to "1.4.8"
                )
        )
    }
}

tasks.build {
    dependsOn("asciidoctor")
}