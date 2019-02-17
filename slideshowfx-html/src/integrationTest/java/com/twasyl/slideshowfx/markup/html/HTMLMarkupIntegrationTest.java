package com.twasyl.slideshowfx.markup.html;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.plugin.BasePluginIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("The HTML plugin")
public class HTMLMarkupIntegrationTest extends BasePluginIntegrationTest {

    @Test
    @DisplayName("can be installed in the OSGi framework")
    void pluginIsInstalled() {
        List<IMarkup> installedServices = OSGiManager.getInstance().getInstalledServices(IMarkup.class);
        assertNotNull(installedServices);
        assertEquals(1, installedServices.size());
        assertEquals("HTML", installedServices.get(0).getName());
    }

    @Test
    @DisplayName("can convert some text")
    void convertText() {
        List<IMarkup> installedServices = OSGiManager.getInstance().getInstalledServices(IMarkup.class);

        final String html = installedServices.get(0).convertAsHtml("<h1>Hello</h1>");
        assertEquals("<h1>Hello</h1>", html);
    }
}
