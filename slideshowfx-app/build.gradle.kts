import com.twasyl.slideshowfx.gradle.plugins.documentation.Documentation.RENDER_DOCUMENTATION_TASK_NAME
import com.twasyl.slideshowfx.gradle.plugins.documentation.tasks.RenderDocumentation
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

plugins {
    java
    id("org.openjfx.javafxplugin")
    jacoco
    id("org.sonarqube")
    id("sfx-packager")
}

description = "The SlideshowFX application"
version = project.findProperty("productVersion") ?: System.getenv("PRODUCT_VERSION") ?: "2020.1"

dependencies {
    implementation(project(":slideshowfx-content-extension"))
    implementation(project(":slideshowfx-engines"))
    implementation(project(":slideshowfx-global-configuration"))
    implementation(project(":slideshowfx-hosting-connector"))
    implementation(project(":slideshowfx-icons"))
    implementation(project(":slideshowfx-logs"))
    implementation(project(":slideshowfx-markup"))
    implementation(project(":slideshowfx-plugin-manager"))
    implementation(project(":slideshowfx-plugin"))
    implementation(project(":slideshowfx-server"))
    implementation(project(":slideshowfx-snippet-executor"))
    implementation(project(":slideshowfx-style"))
    implementation(project(":slideshowfx-ui-controls"))
    implementation(project(":slideshowfx-utils"))

    implementation(group = "com.google.zxing", name = "javase", version = project.property("dependencies.zxing.version") as String)

    testImplementation(project(":slideshowfx-server"))
    testImplementation(group = "org.mockito", name = "mockito-core", version = project.property("dependencies.mockito.version") as String)
}

packaging {
    outputDir = file("${getBuildDir()}/package")
    executableBaseName = "SlideshowFX"

    runtime.modules = listOf("java.desktop", "java.logging", "java.net.http", "java.scripting", "java.sql", "java.xml", "jdk.jsobject", "jdk.unsupported", "jdk.unsupported.desktop", "jdk.xml.dom")
    runtime.jlinkOptions = listOf("--no-header-files",
            "--no-man-pages",
            "--compress=0",
            "--strip-debug",
            "--strip-native-commands")

    app.jvmOpts = listOf("-Xms512m",
            "-Xmx2g",
            "--enable-preview",
            "-Dfile.encoding=UTF-8",
            "-Duser.language=en",
            "-Djava.util.logging.config.file=@@LOGGING_CONFIGURATION_FILE@@",
            "-Djavafx.preloader=com.twasyl.slideshowfx.app.SlideshowFXPreloader",
            "--add-modules", "ALL-MODULE-PATH")

    app.module = "slideshowfx.app"
    app.mainClass = "com.twasyl.slideshowfx.app.SlideshowFX"
}

javafx {
    modules("javafx.controls", "javafx.fxml", "javafx.web")
}

tasks {
    processResources {
        dependsOn(":slideshowfx-documentation:$RENDER_DOCUMENTATION_TASK_NAME")
        doLast {
            copy {
                from(project(":slideshowfx-documentation").tasks.named<RenderDocumentation>(RENDER_DOCUMENTATION_TASK_NAME).get().outputs)
                into("${sourceSets["main"].output.resourcesDir!!.absolutePath}/com/twasyl/slideshowfx/documentation/html")
            }

            if (plugins.hasPlugin(IdeaPlugin::class)) {
                val out = plugins.getPlugin(IdeaPlugin::class).model.module.outputDir
                copy {
                    from(project(":slideshowfx-documentation").tasks.named<RenderDocumentation>(RENDER_DOCUMENTATION_TASK_NAME).get().outputs)
                    into("${out}/com/twasyl/slideshowfx/documentation")
                }
            }
        }
    }

    register<Zip>("zipSlideContentEditor") {
        archiveFileName.set("sfx-slide-content-editor.zip")
        destinationDirectory.set(file("${buildDir}/tmp"))
        from("${buildDir}/tmp/sfx-slide-content-editor-update/sfx-slide-content-editor") {
            into("sfx-slide-content-editor")
        }
    }

    register("createSlideContentEditor") {
        finalizedBy("zipSlideContentEditor")

        doLast {
            val aceVersion = project.findProperty("aceVersion") as String ?: ""
            val binaryDir = File("${buildDir}/tmp/sfx-slide-content-editor-update")
            val binary = File("${binaryDir}/binary-${aceVersion}.zip")
            val newEditorDir = File(binaryDir, "sfx-slide-content-editor")
            val unpackedBinary = File("${binaryDir}/ace-builds-${aceVersion}")

            if (!binaryDir.exists()) {
                binaryDir.mkdirs()
            }

            URL("https://github.com/ajaxorg/ace-builds/archive/v${aceVersion}.zip")
                    .openStream()
                    .use { `in` -> Files.copy(`in`, binary.toPath(), REPLACE_EXISTING) }

            copy {
                from(zipTree(binary))
                into(binaryDir)
            }

            binary.delete()

            if (newEditorDir.exists()) {
                newEditorDir.deleteRecursively()
            }

            copy {
                from(fileTree("${unpackedBinary}/src-min-noconflict"))
                include("ace.js", "snippets/asciidoc.js", "snippets/html*.js", "snippets/markdown.js", "snippets/textile.js", "ext-language_tools.js", "ext-searchbox.js", "ext-static_highlight.js", "ext-whitespace.js", "keybinding-*.js", "mode-asciidoc.js", "mode-html.js", "mode-markdown.js", "mode-textile.js", "theme-tomorrow_night.js", "theme-xcode.js", "worker-*.js")
                into(file("${newEditorDir}/ace/${aceVersion}"))
            }

            unpackedBinary.deleteRecursively()

            File(newEditorDir, "ace-file-editor.html").writeText("""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <style type="text/css" media="screen">
        body {
            overflow: hidden;
        }

        #editor {
            margin: 0;
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
        }
    </style>
    <script type="text/javascript">
        // Set the content of the editor. The content is given in Base64
        function setContent(content) {
            var editor = ace.edit('editor');
            editor.setValue(decodeURIComponent(escape(window.atob(content))));
            editor.clearSelection();
            editor.resize();
        }

        // Insert the content where the cursor is
        function appendContent(content) {
            var editor = ace.edit('editor');
            editor.insert(decodeURIComponent(escape(window.atob(content))));
            editor.clearSelection();
            editor.resize();
        }

        // Get the content of the editor. The content is returned in Base64
        function getContent() {
            var editor = ace.edit('editor');
            return window.btoa(unescape(encodeURIComponent(editor.getValue())));
        }

        // Get the selected content of the editor. The content is returned in Base64
        function getSelectedContent() {
            var editor = ace.edit('editor');
            return window.btoa(unescape(encodeURIComponent(editor.getCopyText())));
        }

        // Remove the selection
        function removeSelection() {
            var editor = ace.edit('editor');
            editor.removeLines();
        }

        // Set the mode of this editor
        function setMode(newMode) {
            var editor = ace.edit('editor');
            editor.getSession().setMode(newMode);
        }

        // Select all text in the editor
        function selectAll() {
            var editor = ace.edit('editor');
            editor.selectAll();
        }

        // Request the focus in the editor
        function requestEditorFocus() {
            var editor = ace.edit('editor');
            editor.focus();
        }

        // Change the font size of the editor. If the factor is greater than zero, the font
        // is increased, if less than zero decreased, equal to zero nothing is done.
        function changeFontSize(factor) {
            var changeFontSizeBy = undefined;

            if(factor > 0) changeFontSizeBy = 1;
            else if(factor < 0) changeFontSizeBy = -1;

            if(changeFontSizeBy !== undefined) {
                var editor = ace.edit('editor');
                var currentFontSize = editor.getFontSize();

                editor.setFontSize(currentFontSize + changeFontSizeBy);
            }
        }
        
        // Change the theme of the editor.
        function changeTheme(theme) {
            var editor = ace.edit('editor');
            editor.setTheme("ace/theme/" + theme);
        }
    </script>
    <script src="ace/${aceVersion}/ace.js"></script>
</head>
<body>
    <pre id='editor'></pre>

    <script type="text/javascript">
        var editor = ace.edit('editor');
        editor.getSession().setUseWrapMode(true);
        changeTheme('tomorrow_night');
    </script>
</body>
</html>
""")
        }
    }

    jar {
        manifest {
            attributes(
                    "Implementation-Title" to "SlideshowFX",
                    "Main-Class" to "com.twasyl.slideshowfx.app.SlideshowFX",
                    "JavaFX-Preloader-Class" to "com.twasyl.slideshowfx.app.SlideshowFXPreloader",
                    "JavaFX-Application-Class" to "com.twasyl.slideshowfx.app.SlideshowFX",
                    "JavaFX-Version" to "14+"
            )
        }
    }

    prepareResources {
        dependsOn(
                ":slideshowfx-content-extension:jar",
                ":slideshowfx-documentation:$RENDER_DOCUMENTATION_TASK_NAME",
                ":slideshowfx-engines:jar",
                ":slideshowfx-global-configuration:jar",
                ":slideshowfx-hosting-connector:jar",
                ":slideshowfx-icons:jar",
                ":slideshowfx-logs:jar",
                ":slideshowfx-markup:jar",
                ":slideshowfx-plugin:jar",
                ":slideshowfx-plugin-manager:jar",
                ":slideshowfx-server:jar",
                ":slideshowfx-snippet-executor:jar",
                ":slideshowfx-style:jar",
                ":slideshowfx-ui-controls:jar",
                ":slideshowfx-utils:jar")
    }

    build {
        dependsOn("createPackage")
    }

    test {
        configure<JacocoTaskExtension> {
            excludes = listOf("com.twasyl.slideshowfx.controllers.*", "com.twasyl.slideshowfx.controls.*", "com.twasyl.slideshowfx.app.*")
        }
    }
}