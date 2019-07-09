package com.twasyl.slideshowfx.markup.asciidoctor;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import com.twasyl.slideshowfx.plugin.manager.PluginManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("The asciidoctor plugin")
public class AsciidoctorMarkupIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the plugin manager")
    void pluginIsInstalled() {
        assertPluginIsInstalled(IMarkup.class, "asciidoctor");
    }

    @Test
    @DisplayName("can convert some text")
    void convertText() {
        List<IMarkup> installedServices = PluginManager.getInstance().getServices(IMarkup.class);

        final String html = installedServices.get(0).convertAsHtml("== Hello");
        assertEquals("<div class=\"sect1\">\n<h2>Hello</h2>\n<div class=\"sectionbody\">\n\n</div>\n</div>", html);
    }
}
