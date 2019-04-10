package com.twasyl.slideshowfx.content.extension.image;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Image content extension' plugin")
public class ImageContentExtensionIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IContentExtension.class, "IMAGE");
    }
}
