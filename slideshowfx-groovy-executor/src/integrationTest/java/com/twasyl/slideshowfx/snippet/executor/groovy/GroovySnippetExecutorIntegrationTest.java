package com.twasyl.slideshowfx.snippet.executor.groovy;

import com.twasyl.slideshowfx.plugin.manager.BasePluginIntegrationTest;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Groovy snippet executor' plugin")
public class GroovySnippetExecutorIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(ISnippetExecutor.class, "GROOVY");
    }
}
