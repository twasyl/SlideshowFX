package com.twasyl.slideshowfx.markup.textile;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("The textile plugin")
public class TextileMarkupIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IMarkup.class, "Textile");
    }

    @Test
    @DisplayName("can convert some text")
    void convertText() {
        List<IMarkup> installedServices = PluginManager.getInstance().getServices(IMarkup.class);

        final String html = installedServices.get(0).convertAsHtml("h1. Hello");
        assertEquals("<h1 id=\"Hello\">Hello</h1>", html);
    }
}
