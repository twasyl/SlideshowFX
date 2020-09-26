package com.twasyl.slideshowfx.gradle.plugins.documentation.tasks;

import com.twasyl.slideshowfx.gradle.plugins.documentation.extensions.DocumentationExtension;
import com.twasyl.slideshowfx.gradle.plugins.documentation.internal.FontAwesomeExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Task rendering the expanded documentation to HTML. The task will automatically embed all JavaScript and CSS file
 * into each generated HTML file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class RenderDocumentation extends DefaultTask {
    private final DocumentationExtension extension;

    @Inject
    public RenderDocumentation(final DocumentationExtension extension) {
        this.setDescription("Renders the Markdown documentation to HTML.");
        this.setGroup("build");

        this.extension = extension;

        this.getInputs().dir(this.extension.getExpandDir());

        if (this.extension.getCssDir().get().getAsFile().exists()) {
            this.getInputs().dir(this.extension.getCssDir());
        }
        if (this.extension.getJsDir().get().getAsFile().exists()) {
            this.getInputs().dir(this.extension.getJsDir());
        }
        if (this.extension.getImagesDir().get().getAsFile().exists()) {
            this.getInputs().dir(this.extension.getImagesDir());
        }

        this.getOutputs().dir(this.extension.getRenderDir());
    }

    @TaskAction
    public void render() {
        if (!this.extension.getRenderDir().get().getAsFile().exists()) {
            this.extension.getRenderDir().get().getAsFile().mkdirs();
        }

        try {
            copyCssResources();
            copyJsResources();
            copyImagesResources();
            copyFontsResources();
        } catch (IOException e) {
            throw new GradleException("Error copying the resources", e);
        }

        this.extension.getExpandDir()
                .get()
                .getAsFileTree()
                .matching(action -> action.include("*.md"))
                .forEach(file -> {
                    try {
                        generateHTMLFile(file);
                    } catch (IOException e) {
                        throw new GradleException("Error rendering documentation file " + file.getName(), e);
                    }
                });
    }

    /**
     * Copy the CSS static resources used by the documentation to the final destination.
     *
     * @throws IOException if a CSS file can't be copied.
     */
    private void copyCssResources() throws IOException {
        List<File> cssFiles = cssFiles();
        if (!cssFiles.isEmpty()) {
            final File cssDir = getOrCreateCssResourcesDir();

            for (final var file : cssFiles) {
                final var copy = new File(cssDir, file.getName());
                Files.copy(file.toPath(), copy.toPath(), REPLACE_EXISTING);
            }
        }
    }

    /**
     * Copy the JS static resources used by the documentation to the final destination.
     *
     * @throws IOException if a JS file can't be copied.
     */
    private void copyJsResources() throws IOException {
        List<File> jsFiles = jsFiles();
        if (!jsFiles.isEmpty()) {
            final File jsDir = getOrCreateJsResourcesDir();

            for (final var file : jsFiles) {
                final var copy = new File(jsDir, file.getName());
                Files.copy(file.toPath(), copy.toPath(), REPLACE_EXISTING);
            }
        }
    }

    /**
     * Copy the images static resources used by the documentation to the final destination.
     *
     * @throws IOException if a images file can't be copied.
     */
    private void copyImagesResources() throws IOException {
        final var imagesFiles = imagesFiles();
        if (!imagesFiles.isEmpty()) {
            final var imagesDir = getOrCreateImagesResourcesDir();

            for (final var file : imagesFiles) {
                final var copy = new File(imagesDir, file.getName());
                Files.copy(file.toPath(), copy.toPath(), REPLACE_EXISTING);
            }
        }
    }

    /**
     * Copy the fonts static resources used by the documentation to the final destination.
     *
     * @throws IOException if a fonts file can't be copied.
     */
    private void copyFontsResources() throws IOException {
        final var fontsFiles = fontsFiles();
        if (!fontsFiles.isEmpty()) {
            final File fontsDir = getOrCreateFontsResourcesDir();

            for (final var file : fontsFiles) {
                final var copy = new File(fontsDir, file.getName());
                Files.copy(file.toPath(), copy.toPath(), REPLACE_EXISTING);
            }
        }
    }

    /**
     * Generates the HTML for the source documentation file.
     *
     * @param sourceFile The source documentation to convert to HTML
     * @throws IOException
     */
    private void generateHTMLFile(final File sourceFile) throws IOException {
        final var output = new File(this.extension.getRenderDir().get().getAsFile(), sourceFile.getName().replace(".md", ".html"));

        try (final FileWriter writer = new FileWriter(output, UTF_8)) {
            writer.write(convertToHTML(sourceFile));
        }
    }

    /**
     * Make sure the CSS resources' directory where the CSS files used by the documentation will be copied exists. If
     * the directory doesn't exist, it will be created.
     *
     * @return The CSS resources' directory where the CSS resources used by the documentation will be copied.
     */
    private File getOrCreateCssResourcesDir() {
        final var cssDir = new File(getOrCreateResourcesDir(), this.extension.getCssDir().get().getAsFile().getName());
        if (!cssDir.exists()) {
            cssDir.mkdirs();
        }

        return cssDir;
    }

    /**
     * Make sure the JS resources' directory where the JS files used by the documentation will be copied exists. If
     * the directory doesn't exist, it will be created.
     *
     * @return The JS resources' directory where the JS resources used by the documentation will be copied.
     */
    private File getOrCreateJsResourcesDir() {
        final var jsDir = new File(getOrCreateResourcesDir(), this.extension.getJsDir().get().getAsFile().getName());
        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }

        return jsDir;
    }

    /**
     * Make sure the images resources' directory where the images files used by the documentation will be copied exists.
     * If the directory doesn't exist, it will be created.
     *
     * @return The images resources' directory where the images resources used by the documentation will be copied.
     */
    private File getOrCreateImagesResourcesDir() {
        final var imagesDir = new File(getOrCreateResourcesDir(), this.extension.getImagesDir().get().getAsFile().getName());
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        return imagesDir;
    }

    /**
     * Make sure the fonts resources' directory where the fonts files used by the documentation will be copied exists. If
     * the directory doesn't exist, it will be created.
     *
     * @return The fonts resources' directory where the fonts resources used by the documentation will be copied.
     */
    private File getOrCreateFontsResourcesDir() {
        final var fontsDir = new File(getOrCreateResourcesDir(), this.extension.getFontsDir().get().getAsFile().getName());
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }

        return fontsDir;
    }

    /**
     * Make sure the resources' directory where the static resources used by the documentation will be copied exists. If
     * the directory doesn't exist, it will be created.
     *
     * @return The resources' directory where the static resources used by the documentation will be copied.
     */
    private File getOrCreateResourcesDir() {
        final var resourcesDir = new File(this.extension.getRenderDir().get().getAsFile(), "resources");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }

        return resourcesDir;
    }

    /**
     * Converts the given source file to an HTML document.
     *
     * @param file The source document to convert to HTML
     * @return The full HTML document, ready to be written in a file.
     */
    private String convertToHTML(final File file) {
        final var document = Jsoup.parse("<html><head></head><body></body></html>");
        document.head().appendElement("meta").attr("charset", "UTF-8");

        cssFiles().forEach(cssFile -> {
            final var cssPath = new File(getOrCreateCssResourcesDir(), cssFile.getName()).toPath();
            var href = this.extension.getRenderDir().get().getAsFile().toPath().relativize(cssPath).toString();
            href = href.replaceAll("\\\\", "/");
            document.head().appendElement("link")
                    .attr("rel", "stylesheet")
                    .attr("type", "text/css")
                    .attr("href", href);
        });

        jsFiles().forEach(jsFile -> {
            final var jsPath = new File(getOrCreateJsResourcesDir(), jsFile.getName()).toPath();
            var src = this.extension.getRenderDir().get().getAsFile().toPath().relativize(jsPath).toString();
            src = src.replaceAll("\\\\", "/");
            document.head().appendElement("script")
                    .attr("type", "text/javascript")
                    .attr("src", src);
        });

        document.body().append(parseMarkdown(file));
        return document.html();
    }

    /**
     * Parses the given markdown file and converts it to an HTML fragment. This fragment can then be included within a
     * HTML document.
     *
     * @param file The file to parse.
     * @return The HTML content corresponding to the content of the given markdown file.
     */
    private String parseMarkdown(final File file) {
        try (final var reader = new FileReader(file, UTF_8)) {
            final var extensions = List.of(TablesExtension.create(), FontAwesomeExtension.create());
            final Parser parser = Parser.builder().extensions(extensions).build();
            final Node node = parser.parseReader(reader);
            final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
            return renderer.render(node).trim();
        } catch (IOException e) {
            throw new GradleException("Error rendering documentation file " + file.getName(), e);
        }
    }

    /**
     * Get the list of all CSS files used by the documentation.
     *
     * @return The list of CSS files
     */
    private List<File> cssFiles() {
        if (this.extension.getCssDir().get().getAsFile().exists()) {
            return Stream.of(this.extension.getCssDir().get().getAsFile()
                    .listFiles((dir, name) -> name.endsWith(".css")))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Get the list of all JavaScript files used by the documentation.
     *
     * @return The list of JavaScript files
     */
    private List<File> jsFiles() {
        if (this.extension.getJsDir().get().getAsFile().exists()) {
            return Stream.of(this.extension.getJsDir().get().getAsFile()
                    .listFiles((dir, name) -> name.endsWith(".js")))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Get the list of all images used by the documentation.
     *
     * @return The list of images
     */
    private List<File> imagesFiles() {
        if (this.extension.getImagesDir().get().getAsFile().exists()) {
            return Stream.of(this.extension.getImagesDir().get().getAsFile()
                    .listFiles())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Get the list of all fonts used by the documentation.
     *
     * @return The list of fonts
     */
    private List<File> fontsFiles() {
        if (this.extension.getFontsDir().get().getAsFile().exists()) {
            return Stream.of(this.extension.getFontsDir().get().getAsFile()
                    .listFiles())
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
