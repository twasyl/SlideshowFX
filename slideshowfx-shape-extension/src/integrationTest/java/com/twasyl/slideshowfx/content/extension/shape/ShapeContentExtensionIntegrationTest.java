package com.twasyl.slideshowfx.content.extension.shape;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Shape content extension' plugin")
public class ShapeContentExtensionIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IContentExtension.class, "SHAPE");
    }
}
