package com.twasyl.slideshowfx.plugin.manager.internal;

import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.plugin.manager.PluginTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class RegisteredPluginTest {

    @BeforeAll
    static void setup() {
        final File pluginsDir = new File("build/tmp", "pluginsDir-" + System.currentTimeMillis());
        System.setProperty("plugins.dir", pluginsDir.getAbsolutePath());
        pluginsDir.mkdirs();
    }

    @Test
    void getNameWhenNotUnzipped() throws IOException {
        final File plugin = PluginTestUtils.createDummyPlugin("my-plugin", "Awesome plugin", "1.0");
        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(plugin));

        assertEquals("Awesome plugin", registeredPlugin.getName());
    }

    @Test
    void getVersionWhenNotUnzipped() throws IOException {
        final File plugin = PluginTestUtils.createDummyPlugin("my-plugin", "Awesome plugin", "1.0");
        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(plugin));

        assertEquals("1.0", registeredPlugin.getVersion());
    }

    @Test
    void getDescriptionWhenNotUnzipped() throws IOException {
        final File plugin = PluginTestUtils.createDummyPlugin("my-plugin", "Awesome plugin", "1.0");
        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(plugin));

        final String description = registeredPlugin.getDescription();
        assertTrue(description.startsWith("This is a dummy plugin"), "Description is: " + description);
    }

    @Test
    void pluginIsInstanceOf() throws IOException {
        final File plugin = PluginTestUtils.createDummyPlugin("my-plugin", "Awesome plugin", "1.0");
        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(plugin));

        assertTrue(registeredPlugin.isInstanceOf(IPlugin.class));
    }

    @Test
    void pluginIsNotInstanceOf() throws IOException {
        final File plugin = PluginTestUtils.createDummyPlugin("my-plugin", "Awesome plugin", "1.0");
        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(plugin));

        assertFalse(registeredPlugin.isInstanceOf(AbstractPlugin.class));
    }
}
