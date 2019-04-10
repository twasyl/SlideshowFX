package com.twasyl.slideshowfx.content.extension.sequence.diagram;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Sequence diagram content extension' plugin")
public class SequenceDiagramContentExtensionIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IContentExtension.class, "SEQUENCE_DIAGRAM");
    }
}
