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
                this.configuration = engine.readConfiguration(configurationInput);
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

    public PresentationConfigurationTestContext clearSlides() {
        this.configuration.getSlides().clear();
        return this;
    }

    public PresentationConfigurationTestContext assertHasNoSlides() {
        assertFalse(this.configuration.hasSlides());
        return this;
    }

    public PresentationConfigurationTestContext assertHasSlides() {
        assertTrue(this.configuration.hasSlides());
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

    public PresentationConfigurationTestContext assertSpeakerNotes(final String expectedSpeakerNotes) {
        if (expectedSpeakerNotes == null) {
            assertAll("Assertions for testing the absence of speaker notes for slide " + this.slide.getId() + " have failed",
                    () -> assertNull(this.slide.getSpeakerNotes()),
                    () -> assertFalse(this.slide.hasSpeakerNotes()));
        } else {
            assertAll("Assertions for testing the presence of speaker notes for slide " + this.slide.getId() + " have failed",
                    () -> assertNotNull(this.slide.getSpeakerNotes()),
                    () -> assertTrue(this.slide.hasSpeakerNotes()),
                    () -> assertEquals(expectedSpeakerNotes, this.slide.getSpeakerNotes()));
        }

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

    public void assertNumberOfVariables(final int expectedNumber) {
        assertAll(
                () -> assertNotNull(this.configuration.getVariables()),
                () -> assertEquals(expectedNumber, this.configuration.getVariables().size()));
    }

    public PresentationConfigurationTestContext assertHasVariable(final Variable expectedVariable) {
        assertNotNull(this.configuration.getVariables()
                .stream()
                .filter(expectedVariable::equals)
                .findFirst()
                .orElse(null));
        return this;
    }

    public PresentationConfigurationTestContext assertNextSlideIs(final String expectedSiblingSlideId) {
        final Slide slideAfter = this.configuration.getSlideAfter(this.slide.getSlideNumber());

        if ("none".equals(expectedSiblingSlideId)) {
            assertNull(slideAfter, "The next slide of slide ID " + this.slide.getId() + " is not null");
        } else {
            assertNotNull(slideAfter, "The next slide of slide ID " + this.slide.getId() + " is null");
            assertEquals(expectedSiblingSlideId, slideAfter.getId());
        }
        return this;
    }

    public PresentationConfigurationTestContext assertPreviousSlideIs(final String expectedSiblingSlideId) {
        final Slide slideBefore = this.configuration.getSlideBefore(this.slide.getSlideNumber());

        if ("none".equals(expectedSiblingSlideId)) {
            assertNull(slideBefore, "The previous slide of slide ID " + this.slide.getId() + " is not null");
        } else {
            assertNotNull(slideBefore, "The previous slide of slide ID " + this.slide.getId() + " is null");
            assertEquals(expectedSiblingSlideId, slideBefore.getId());
        }
        return this;
    }

    public PresentationConfigurationTestContext assertFirstSlide(final String expectedSlideId) {
        final Slide firstSlide = this.configuration.getFirstSlide();

        if ("none".equals(expectedSlideId)) {
            assertNull(firstSlide, "The first slide is not null");
        } else {
            assertNotNull(firstSlide, "The first slide is null");
            assertEquals(expectedSlideId, firstSlide.getId(), "The first slide doesn't have the ID " + expectedSlideId);
        }
        return this;
    }

    public PresentationConfigurationTestContext assertLastSlide(final String expectedSlideId) {
        final Slide lastSlide = this.configuration.getLastSlide();

        if ("none".equals(expectedSlideId)) {
            assertNull(lastSlide, "The last slide is not null");
        } else {
            assertNotNull(lastSlide, "The last slide is null");
            assertEquals(expectedSlideId, lastSlide.getId(), "The last slide doesn't have the ID " + expectedSlideId);
        }
        return this;
    }
}
