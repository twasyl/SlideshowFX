package com.twasyl.slideshowfx.engine;

import com.twasyl.slideshowfx.utils.io.IOUtils;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.fail;

public class EngineTestUtils {

    private EngineTestUtils() {
    }

    public static JsonObject createTemplateConfiguration() {
        try (final InputStream content = EngineTestUtils.class.getResourceAsStream("template-config.json")) {
            return new JsonObject(IOUtils.read(content));
        } catch (IOException e) {
            fail(e);
        }
        return null;
    }

    public static JsonObject createPresentationConfiguration() {
        try (final InputStream content = EngineTestUtils.class.getResourceAsStream("presentation-config.json")) {
            return new JsonObject(IOUtils.read(content));
        } catch (IOException e) {
            fail(e);
        }
        return null;
    }

    private static void appendFile(final ZipOutputStream output, final String resource) throws IOException {
        output.write(IOUtils.read(EngineTestUtils.class.getResourceAsStream(resource)).getBytes());
    }

    private static void appendTemplateConfig(final ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("template-config.json"));
        output.write(createTemplateConfiguration().encodePrettily().getBytes());
        output.closeEntry();
    }

    private static void appendPresentationConfig(final ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("presentation-config.json"));
        output.write(createPresentationConfiguration().encodePrettily().getBytes());
        output.closeEntry();
    }

    private static void appendTemplateFile(final ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("template.html"));
        appendFile(output, "template.html");
        output.closeEntry();
    }

    private static void appendSample(final ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("sample.html"));
        appendFile(output, "sample.html");
        output.closeEntry();
    }

    private static void appendResources(final ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("resources/"));
        output.closeEntry();
    }

    private static void appendSlideTemplates(ZipOutputStream output) throws IOException {
        output.putNextEntry(new ZipEntry("slides/template/title.html"));
        output.write("<section id=\"slide-${slideNumber}\"><h1 id=\"${slideNumber}-title\">Title</h1><h2 id=\"${slideNumber}-subtitle\"></h2></section>".getBytes());
        output.closeEntry();

        output.putNextEntry(new ZipEntry("slides/template/regular.html"));
        output.write("<section id=\"slide-${slideNumber}\"><h1 id=\"${slideNumber}-title\">Title</h1><div id=\"${slideNumber}-content\"></div></section>".getBytes());
        output.closeEntry();
    }

    public static File createTemplateArchive() {
        final File parent = new File("build/test-execution");
        if (!parent.exists()) {
            parent.mkdirs();
        }

        final File file = new File(parent, currentTimeMillis() + ".sfxt");

        try (final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
            appendTemplateConfig(output);
            appendTemplateFile(output);
            appendSample(output);
            appendResources(output);
            appendSlideTemplates(output);
        } catch (IOException e) {
            fail(e);
        }

        return file;
    }

    public static File createPresentationArchive() {
        final File parent = new File("build/test-execution");
        if (!parent.exists()) {
            parent.mkdirs();
        }

        final File file = new File(parent, currentTimeMillis() + ".sfx");

        try (final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
            appendTemplateConfig(output);
            appendTemplateFile(output);
            appendPresentationConfig(output);
            appendSample(output);
            appendResources(output);
            appendSlideTemplates(output);
        } catch (IOException e) {
            fail(e);
        }

        return file;
    }
}
