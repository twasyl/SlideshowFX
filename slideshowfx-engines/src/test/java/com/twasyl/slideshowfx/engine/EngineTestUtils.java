package com.twasyl.slideshowfx.engine;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.twasyl.slideshowfx.engine.template.configuration.TemplateConfigurationFields.*;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.fail;

public class EngineTestUtils {

    private EngineTestUtils() {
    }

    public static JsonObject createTemplateConfiguration() {
        return new JsonObject()
                .put(TEMPLATE.getFieldName(), new JsonObject()
                        .put(TEMPLATE_NAME.getFieldName(), "My template")
                        .put(TEMPLATE_VERSION.getFieldName(), "0.1")
                        .put(TEMPLATE_FILE.getFieldName(), "template.html")
                        .put(JS_OBJECT.getFieldName(), "sfx")
                        .put(TEMPLATE_RESOURCES_DIRECTORY.getFieldName(), "resources")
                        .put(TEMPLATE_DEFAULT_VARIABLES.getFieldName(), new JsonArray()
                                .add(new JsonObject()
                                        .put(TEMPLATE_DEFAULT_VARIABLE_NAME.getFieldName(), "author")
                                        .put(TEMPLATE_DEFAULT_VARIABLE_VALUE.getFieldName(), Base64.getEncoder().encodeToString("Someone".getBytes()))))
                        .put(SLIDES.getFieldName(), new JsonObject()
                                .put(SLIDES_CONFIGURATION.getFieldName(), new JsonObject()
                                        .put(SLIDES_CONTAINER.getFieldName(), "slides")
                                        .put(SLIDE_ID_PREFIX.getFieldName(), "slide-")
                                        .put(SLIDES_TEMPLATE_DIRECTORY.getFieldName(), "slides/template")
                                        .put(SLIDES_PRESENTATION_DIRECTORY.getFieldName(), "slides/presentation"))
                                .put(SLIDES_DEFINITION.getFieldName(), new JsonArray()
                                        .add(new JsonObject()
                                                .put(SLIDE_ID.getFieldName(), 1)
                                                .put(SLIDE_NAME.getFieldName(), "Title")
                                                .put(SLIDE_FILE.getFieldName(), "title.html")
                                                .put(SLIDE_ELEMENTS.getFieldName(), new JsonArray()
                                                        .add(new JsonObject()
                                                                .put(SLIDE_ELEMENT_ID.getFieldName(), 1)
                                                                .put(SLIDE_ELEMENT_HTML_ID.getFieldName(), "${slideNumber}-title")
                                                                .put(SLIDE_ELEMENT_DEFAULT_CONTENT.getFieldName(), "Title"))
                                                        .add(new JsonObject()
                                                                .put(SLIDE_ELEMENT_ID.getFieldName(), 2)
                                                                .put(SLIDE_ELEMENT_HTML_ID.getFieldName(), "${slideNumber}-subtitle")
                                                                .put(SLIDE_ELEMENT_DEFAULT_CONTENT.getFieldName(), "Subtitle")))))));
    }

    public static File createTemplateArchive() {
        final File parent = new File("build/test-execution");
        if (!parent.exists()) {
            parent.mkdirs();
        }

        final File file = new File(parent, currentTimeMillis() + ".zip");

        try (final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(file))) {
            output.putNextEntry(new ZipEntry("template-config.json"));
            output.write(createTemplateConfiguration().encodePrettily().getBytes());
            output.closeEntry();

            output.putNextEntry(new ZipEntry("template.html"));
            output.write("<html><head></head><body><div id=\"slides\"></div></body></html>".getBytes());
            output.closeEntry();

            output.putNextEntry(new ZipEntry("sample.html"));
            output.write("<html><head></head><body><div id=\"slides\"></div></body></html>".getBytes());
            output.closeEntry();

            output.putNextEntry(new ZipEntry("resources/"));
            output.closeEntry();

            output.putNextEntry(new ZipEntry("slides/"));
            output.putNextEntry(new ZipEntry("slides/template/"));
            output.putNextEntry(new ZipEntry("slides/presentation/"));

            output.closeEntry();
            output.closeEntry();
            output.closeEntry();
        } catch (IOException e) {
            fail(e);
        }

        return file;
    }
}
