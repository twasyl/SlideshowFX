package com.twasyl.slideshowfx.engine.context;

import com.twasyl.slideshowfx.content.extension.Resource;
import com.twasyl.slideshowfx.engine.EngineTestUtils;
import com.twasyl.slideshowfx.engine.Variable;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.presentation.configuration.PresentationConfiguration;
import com.twasyl.slideshowfx.engine.presentation.configuration.Slide;
import com.twasyl.slideshowfx.engine.presentation.configuration.SlideElement;
import com.twasyl.slideshowfx.utils.io.DefaultCharsetReader;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class PresentationConfigurationTestContext extends AbstractConfigurationTestContext<PresentationConfiguration> {
    private PresentationEngine engine;
    private Slide slide;
    private SlideElement slideElement;

    public void loadPresentation() {
        engine = new PresentationEngine();
        final File archive = EngineTestUtils.createPresentationArchive();

        assertDoesNotThrow(() -> engine.loadArchive(archive));
    }

    public void loadValidConfiguration() {
        final JsonObject expectedConfiguration = EngineTestUtils.createPresentationConfiguration();

        assertDoesNotThrow(() -> {
            try (final ByteArrayInputStream bytes = new ByteArrayInputStream(expectedConfiguration.encode().getBytes());
                 final DefaultCharsetReader configurationInput = new DefaultCharsetReader(bytes)) {
                configuration = engine.readConfiguration(configurationInput);
            }
        });

        assertConfigurationNotNull();
    }

    public PresentationConfigurationTestContext withSlideId(final String slideId) {
        this.slide = this.configuration.getSlideById(slideId);

        assertAll(
                () -> assertNotNull(this.slide),
                () -> assertEquals(slideId, this.slide.getId()));

        return this;
    }

    public PresentationConfigurationTestContext withSlideElement(final String slideElementId) {
        this.slideElement = this.slide.getElement(slideElementId);

        assertAll(
                () -> assertNotNull(this.slideElement),
                () -> assertEquals(slideElementId, this.slideElement.getId()));

        return this;
    }

    public PresentationConfigurationTestContext assertFieldEquals(final String fieldName, final String expectedValue) {
        if ("presentation id".equals(fieldName)) {
            assertEquals(Long.parseLong(expectedValue), this.configuration.getId());
        } else {
            fail("Unknown field '" + fieldName + "'");
        }
        return this;
    }

    public PresentationConfigurationTestContext assertNumberOfSlides(final int expectedNumberOfSlides) {
        assertAll(
                () -> assertNotNull(this.configuration.getSlides()),
                () -> assertEquals(expectedNumberOfSlides, this.configuration.getSlides().size()));
        return this;
    }

    public PresentationConfigurationTestContext assertNumberOfSlideElements(final int expectedNumberOfSlideElements) {
        assertAll(
                () -> assertNotNull(this.slide.getElements()),
                () -> assertEquals(expectedNumberOfSlideElements, this.slide.getElements().size()));
        return this;
    }

    public PresentationConfigurationTestContext assertSlideTemplateId(final int expectedTemplateId) {
        assertAll(
                () -> assertNotNull(this.slide.getTemplate()),
                () -> assertEquals(expectedTemplateId, this.slide.getTemplate().getId()));
        return this;
    }

    public PresentationConfigurationTestContext assertSlideNumber(final String expectedSlideNumber) {
        assertEquals(expectedSlideNumber, this.slide.getSlideNumber());
        return this;
    }

    public PresentationConfigurationTestContext assertSlideElementTemplateId(final int expectedSlideElementTemplateId) {
        assertAll(
                () -> assertNotNull(this.slideElement.getTemplate()),
                () -> assertEquals(expectedSlideElementTemplateId, this.slideElement.getTemplate().getId()));
        return this;
    }

    public PresentationConfigurationTestContext assertOriginalContentCode(final String expectedOriginalContentCode) {
        assertEquals(expectedOriginalContentCode, this.slideElement.getOriginalContentCode());
        return this;
    }

    public PresentationConfigurationTestContext assertOriginalContent(final String expectedOriginalContent) {
        assertEquals(expectedOriginalContent, this.slideElement.getOriginalContent());
        return this;
    }

    public PresentationConfigurationTestContext assertHtmlContent(final String expectedHtmlContent) {
        assertEquals(expectedHtmlContent, this.slideElement.getHtmlContent());
        return this;
    }

    public PresentationConfigurationTestContext assertHasCustomResource(final Resource expectedCustomResource) {
        assertNotNull(this.configuration.getCustomResources()
                .stream()
                .filter(expectedCustomResource::equals)
                .findFirst()
                .orElse(null));
        return this;
    }

    public PresentationConfigurationTestContext assertHasVariable(final Variable expectedVariable) {
        assertNotNull(this.configuration.getVariables()
                .stream()
                .filter(expectedVariable::equals)
                .findFirst()
                .orElse(null));
        return this;
    }
}
