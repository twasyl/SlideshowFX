package com.twasyl.slideshowfx.engine.template.configuration;

import org.junit.jupiter.api.Test;

import static com.twasyl.slideshowfx.engine.template.configuration.TemplateConfigurationFields.TEMPLATE;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateConfigurationFieldsTest {

    @Test
    void fromValidFieldName() {
        final TemplateConfigurationFields actualField = TemplateConfigurationFields.fromFieldName(TEMPLATE.getFieldName());
        assertNotNull(actualField);
        assertEquals(TEMPLATE, actualField);
    }

    @Test
    void fromInvalidFieldName() {
        assertNull(TemplateConfigurationFields.fromFieldName("THIS FIELD DOESN'T EXIST"));
    }
}
