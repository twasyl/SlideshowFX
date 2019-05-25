package com.twasyl.slideshowfx.engine.context;

import com.twasyl.slideshowfx.engine.EngineTestUtils;
import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.engine.template.configuration.SlideElementTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.SlideTemplate;
import com.twasyl.slideshowfx.engine.template.configuration.TemplateConfiguration;
import com.twasyl.slideshowfx.utils.beans.Pair;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateConfigurationTestContext extends AbstractConfigurationTestContext<TemplateConfiguration> {
    private TemplateEngine engine;
    private SlideTemplate slideTemplate;
    private SlideElementTemplate slideElementTemplate;

    public void loadTemplate() {
        engine = new TemplateEngine();
        final File archive = EngineTestUtils.createTemplateArchive();

        assertDoesNotThrow(() -> engine.loadArchive(archive));

    }

    public void loadValidConfiguration() {
        final JsonObject expectedConfiguration = EngineTestUtils.createTemplateConfiguration();

        assertDoesNotThrow(() -> {
            try (final ByteArrayInputStream bytes = new ByteArrayInputStream(expectedConfiguration.encode().getBytes());
                 final DefaultCharsetReader configurationInput = new DefaultCharsetReader(bytes)) {
                configuration = engine.readConfiguration(configurationInput);
            }
        });

        assertConfigurationNotNull();
    }

    public TemplateConfigurationTestContext withSlideTemplateId(final int slideTemplateId) {
        this.slideTemplate = this.configuration.getSlideTemplate(slideTemplateId);
        assertNotNull(this.slideTemplate);
        assertEquals(slideTemplateId, this.slideTemplate.getId());
        return this;
    }

    public TemplateConfigurationTestContext withSlideElementTemplateId(final int slideElementId) {
        this.slideElementTemplate = this.slideTemplate.getSlideElementTemplate(slideElementId);
        assertNotNull(this.slideElementTemplate);
        assertEquals(slideElementId, this.slideElementTemplate.getId());
        return this;
    }

    public void assertFieldEquals(final String fieldName, final String expectedValue) {
        switch (fieldName) {
            case "template name":
                assertEquals(expectedValue, this.configuration.getName());
                break;
            case "template version":
                assertEquals(expectedValue, this.configuration.getVersion());
                break;
            case "template file":
                assertEquals(expectedValue, this.configuration.getFile().getName());
                break;
            case "js-object":
                assertEquals(expectedValue, this.configuration.getJsObject());
                break;
            case "resources directory":
                assertEquals(expectedValue, this.configuration.getResourcesDirectory().getName());
                break;
            case "slides container":
                assertEquals(expectedValue, this.configuration.getSlidesContainer());
                break;
            case "slide ID prefix":
                assertEquals(expectedValue, this.configuration.getSlideIdPrefix());
                break;
            case "slides template directory":
                assertEquals(new File(engine.getWorkingDirectory(), expectedValue), this.configuration.getSlidesTemplateDirectory());
                break;
            case "slides presentation directory":
                assertEquals(new File(engine.getWorkingDirectory(), expectedValue), this.configuration.getSlidesPresentationDirectory());
                break;
            default:
                fail("Unknown field '" + fieldName + "'");
        }
    }

    public void assertNumberOfSlideTemplates(final int expectedNumber) {
        assertAll(
                () -> assertNotNull(this.configuration.getSlideTemplates()),
                () -> assertEquals(expectedNumber, this.configuration.getSlideTemplates().size()));
    }

    public void assertNumberOfDefaultVariables(final int expectedNumber) {
        assertAll(
                () -> assertNotNull(this.configuration.getDefaultVariables()),
                () -> assertEquals(expectedNumber, this.configuration.getDefaultVariables().size()));
    }

    public void assertDefaultVariable(final String expectedVariableName, final String expectedVariableValue) {
        assertNotNull(expectedVariableName);

        final Variable variable = this.configuration.getDefaultVariables()
                .stream()
                .filter(pair -> expectedVariableName.equals(pair.getName()))
                .findAny()
                .orElse(null);

        assertAll(
                () -> assertNotNull(variable, "Variable with name '" + expectedVariableName + "' not found"),
                () -> assertNotNull(variable.getValue()),
                () -> assertEquals(expectedVariableValue, variable.getValue()));
    }

    public void assertSlideTemplateExists() {
        assertNotNull(this.slideTemplate, "The template is null");
    }

    public void assertSlideElementExists() {
        assertNotNull(this.slideElementTemplate);
    }

    public void assertSlideTemplateName(final String expectedName) {
        assertEquals(expectedName, this.slideTemplate.getName());
    }

    public void assertSlideTemplateFile(final String expectedFile) {
        final File slideTemplateFile = new File(this.configuration.getSlidesTemplateDirectory(), expectedFile);
        assertEquals(slideTemplateFile, this.slideTemplate.getFile());
    }

    public void assertNumberOfSlideElements(int expectedSlideElements) {
        assertEquals(expectedSlideElements, this.slideTemplate.getElements().length);
    }

    public void assertSlideElementHtmlId(final String expectedHtmlId) {
        assertEquals(expectedHtmlId, this.slideElementTemplate.getHtmlId());
    }

    public void assertSlideElementDefaultContent(final String defaultContent) {
        assertEquals(defaultContent, this.slideElementTemplate.getDefaultContent());
    }
}
