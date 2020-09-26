import org.gradle.util.GradleVersion

plugins {
     id("documentation")
}

description = "SlideshowFX documentation to be included within the application and the setup package"
version = "1.0"

documentation {
    properties(mutableMapOf(
                "slideshowfx_version" to (project.findProperty("productVersion") ?: System.getenv("PRODUCT_VERSION") ?: "2020.1"),
                "jdk_version" to "15",
                "javafx_version" to "15",
                "gradle_version" to GradleVersion.current().version,
                "asciidoctorj_version" to project.property("dependencies.asciidoctorj.version") as String,
                "commonmark_version" to project.property("dependencies.markdown.version") as String,
                "freemarker_version" to project.property("dependencies.freemarker.version") as String,
                "jsoup_version" to project.property("dependencies.jsoup.version") as String,
                "wikitext_textile_core_version" to project.property("dependencies.wikitext.version") as String,
                "vertx_version" to project.property("dependencies.vertx.version") as String,
                "zxing_jse_version" to project.property("dependencies.zxing.version") as String,
                "box_version" to project.property("dependencies.box.version") as String,
                "drive_version" to project.property("dependencies.drive.version") as String,
                "dropbox_version" to project.property("dependencies.dropbox.version") as String,
                "ace_version" to "1.4.11"))
}