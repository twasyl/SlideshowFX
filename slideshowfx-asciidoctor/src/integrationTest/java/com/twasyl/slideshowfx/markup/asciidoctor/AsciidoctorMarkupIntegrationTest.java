package com.twasyl.slideshowfx.markup.asciidoctor;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("The asciidoctor plugin")
public class AsciidoctorMarkupIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        List<IMarkup> installedServices = OSGiManager.getInstance().getInstalledServices(IMarkup.class);
        assertNotNull(installedServices);
        assertEquals(1, installedServices.size());
        assertEquals("asciidoctor", installedServices.get(0).getName());
    }

    @Test
    @DisplayName("can convert some text")
    void convertText() {
        List<IMarkup> installedServices = OSGiManager.getInstance().getInstalledServices(IMarkup.class);

        final String html = installedServices.get(0).convertAsHtml("== Hello");
        assertEquals("<div class=\"sect1\">\n<h2>Hello</h2>\n<div class=\"sectionbody\">\n\n</div>\n</div>", html);
    }
}
