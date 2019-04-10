package com.twasyl.slideshowfx.snippet.executor.java;

import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Java snippet executor' plugin")
public class JavaSnippetExecutorIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        assertPluginIsInstalled(ISnippetExecutor.class, "JAVA");
    }
}
