package com.twasyl.slideshowfx.snippet.executor.rust;

import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The 'Rust snippet executor' plugin")
public class RustSnippetExecutorIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(ISnippetExecutor.class, "RUST");
    }
}
