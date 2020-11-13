package com.twasyl.slideshowfx.hosting.connector.dropbox;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.plugin.manager.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Dropbox hosting connector' plugin")
public class DropboxHostingConnectorIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IHostingConnector.class, "Dropbox");
    }
}
