package com.twasyl.slideshowfx.engine.presentation.configuration;

/**
 * This enum represents fields that can be present in the configuration file of a presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public enum PresentationConfigurationFields {
    PRESENTATION("presentation"),
    PRESENTATION_ID("id"),
    PRESENTATION_CUSTOM_RESOURCES("custom-resources"),
    CUSTOM_RESOURCE_TYPE("type"),
    CUSTOM_RESOURCE_CONTENT("content"),
    PRESENTATION_VARIABLES("variables"),
    VARIABLE_NAME("name"),
    VARIABLE_VALUE("value"),
    SLIDES("slides"),
    SLIDE_ID("id"),
    SLIDE_NUMBER("number"),
    SLIDE_TEMPLATE_ID("template-id"),
    SLIDE_SPEAKER_NOTES("speaker-notes"),
    SLIDE_ELEMENTS("elements"),
    SLIDE_ELEMENT_TEMPLATE_ID("template-id"),
    SLIDE_ELEMENT_ELEMENT_ID("element-id"),
    SLIDE_ELEMENT_ORIGINAL_CONTENT_CODE("original-content-code"),
    SLIDE_ELEMENT_ORIGINAL_CONTENT("original-content"),
    SLIDE_ELEMENT_HTML_CONTENT("html-content"),
    DEFAULT_PRESENTATION_FILENAME("presentation.html");

    private final String fieldName;

    PresentationConfigurationFields(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
