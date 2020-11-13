package com.twasyl.slideshowfx.markup.html;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import com.twasyl.slideshowfx.plugin.manager.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("The HTML plugin")
public class HTMLMarkupIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IMarkup.class, "HTML");
    }

    @Test
    @DisplayName("can convert some text")
    void convertText() {
        List<IMarkup> installedServices = PluginManager.getInstance().getServices(IMarkup.class);

        final String html = installedServices.get(0).convertAsHtml("<h1>Hello</h1>");
        assertEquals("<h1>Hello</h1>", html);
    }
}
