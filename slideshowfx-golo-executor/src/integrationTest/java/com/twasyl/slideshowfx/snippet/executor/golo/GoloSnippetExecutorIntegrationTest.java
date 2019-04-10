package com.twasyl.slideshowfx.snippet.executor.golo;

import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Golo snippet executor' plugin")
public class GoloSnippetExecutorIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        assertPluginIsInstalled(ISnippetExecutor.class, "GOLO");
    }
}
