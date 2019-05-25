package com.twasyl.slideshowfx.engine.template.configuration;

/**
 * This enum represents fields that can be present in the configuration file of a template.
 *
 * @author Thierry Wasylczenko
 * @version 1.0-SNAPSHOT
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public enum TemplateConfigurationFields {
    TEMPLATE("template"),
    TEMPLATE_NAME("name"),
    TEMPLATE_VERSION("version"),
    TEMPLATE_FILE("file"),
    JS_OBJECT("js-object"),
    TEMPLATE_RESOURCES_DIRECTORY("resources-directory"),
    TEMPLATE_DEFAULT_VARIABLES("default-variables"),
    TEMPLATE_DEFAULT_VARIABLE_NAME("name"),
    TEMPLATE_DEFAULT_VARIABLE_VALUE("value"),
    SLIDES("slides"),
    SLIDES_CONFIGURATION("configuration"),
    SLIDES_TEMPLATE_DIRECTORY("template-directory"),
    SLIDE_ID_PREFIX("slide-id-prefix"),
    SLIDES_CONTAINER("slides-container"),
    SLIDES_DEFINITION("slides-definition"),
    SLIDE_ID("id"),
    SLIDE_NAME("name"),
    SLIDE_FILE("file"),
    SLIDE_DYNAMIC_IDS("dynamic-ids"),
    SLIDE_DYNAMIC_ATTRIBUTES("dynamic-attributes"),
    DYNAMIC_ATTRIBUTE("attribute"),
    DYNAMIC_ATTRIBUTE_PROMPT_MESSAGE("prompt-message"),
    DYNAMIC_ATTRIBUTE_TEMPLATE_EXPRESSION("template-expression"),
    SLIDE_ELEMENTS("elements"),
    SLIDE_ELEMENT_ID("id"),
    SLIDE_ELEMENT_HTML_ID("html-id"),
    SLIDE_ELEMENT_DEFAULT_CONTENT("default-content");

    private final String fieldName;

    TemplateConfigurationFields(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Look for the enum value corresponding to the given {@link #fieldName field name}.
     *
     * @param fieldName The value of {@link #fieldName} to search for.
     * @return The {@link TemplateConfigurationFields} corresponding to the field name, or {@code null} if not found.
     */
    public static TemplateConfigurationFields fromFieldName(final String fieldName) {
        for (TemplateConfigurationFields field : values()) {
            if (field.getFieldName().equals(fieldName)) {
                return field;
            }
        }

        return null;
    }
}
