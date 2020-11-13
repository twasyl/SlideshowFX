package com.twasyl.slideshowfx.plugin.manager.internal;

import com.twasyl.slideshowfx.plugin.manager.PluginTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class PluginClassLoaderTest {
    private static final File TMP_DIR = new File("build/tmp");
    private static PluginFile PLUGIN_FILE;

    @BeforeAll
    static void before() throws IOException {
        final File pluginsDir = new File(TMP_DIR, "pluginsDir-" + System.currentTimeMillis());
        System.setProperty("plugins.dir", pluginsDir.getAbsolutePath());
        pluginsDir.mkdirs();

        PLUGIN_FILE = new PluginFile(PluginTestUtils.createDummyPlugin("dummy", "Dummy plugin", "1.0"));
        PLUGIN_FILE.unarchive();
    }

    @Test
    void testGetClass() throws IOException, ClassNotFoundException {
        final PluginClassLoader pluginClassLoader = PluginClassLoader.newInstance(PLUGIN_FILE);
        final Class<?> clazz = pluginClassLoader.loadClass("com.twasyl.slideshowfx.dummy.plugin.Dummy");

        assertAll(
                () -> assertNotNull(clazz),
                () -> assertEquals("com.twasyl.slideshowfx.dummy.plugin.Dummy", clazz.getName()));
    }

    @Test
    void testGetResource() throws IOException, ClassNotFoundException {
        final PluginClassLoader pluginClassLoader = PluginClassLoader.newInstance(PLUGIN_FILE);

        final Class<?> clazz = pluginClassLoader.loadClass("com.twasyl.slideshowfx.dummy.plugin.Dummy");
        final URL resource = clazz.getResource("/com/twasyl/slideshowfx/dummy/plugin/Application.fxml");
        assertNotNull(resource);
    }
}
