package com.twasyl.slideshowfx.plugin.manager;

import com.twasyl.slideshowfx.plugin.IPlugin;
import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import com.twasyl.slideshowfx.plugin.manager.internal.RegisteredPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.twasyl.slideshowfx.plugin.manager.PluginTestUtils.createDummyPlugin;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link PluginManager} classes
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
class PluginManagerTest {

    private static final File TMP_DIR = new File("build/tmp");

    @BeforeEach
    void before() {
        final File pluginsDir = new File(TMP_DIR, "pluginsDir-" + System.currentTimeMillis());
        System.setProperty("plugins.dir", pluginsDir.getAbsolutePath());
        pluginsDir.mkdirs();

        PluginManager.getInstance().pluginsDirectory = pluginsDir;
        PluginManager.getInstance().start();
    }

    @AfterEach
    void after() {
        PluginManager.getInstance().stop();
    }

    @Test
    void listMostRecentPlugins() {
        final String pluginTimestamp = System.currentTimeMillis() + "";

        final File version12 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.2");
        createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.0");

        final Collection<File> plugins = PluginManager.getInstance().listMostRecentPluginFiles();
        assertAll(
                () -> assertEquals(1, plugins.size()),
                () -> assertTrue(plugins.contains(version12)));
    }

    @Test
    void isMostRecent() throws IOException {
        final String pluginTimestamp = System.currentTimeMillis() + "";

        final File dummyPlugin = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.0");
        PluginManager.getInstance().installPlugin(dummyPlugin);

        RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(dummyPlugin));

        assertNotNull(registeredPlugin);
        assertTrue(PluginManager.getInstance().isPluginMostRecent(registeredPlugin));
    }

    @Test
    void isNotMostRecent() throws IOException {
        final String pluginTimestamp = System.currentTimeMillis() + "";

        final File version1_2 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.2");
        final File version1_0 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.0");

        PluginManager.getInstance().installPlugin(version1_2);
        PluginManager.getInstance().installPlugin(version1_0);

        final RegisteredPlugin registeredPlugin = new RegisteredPlugin(new PluginFile(version1_0));
        assertFalse(PluginManager.getInstance().isPluginMostRecent(registeredPlugin));
    }

    @Test
    void pluginIsInstalledAndStarted() throws IOException {
        final File pluginFile = createDummyPlugin("my-plugin", "My plugin", "1.0");
        PluginManager.getInstance().installPlugin(pluginFile);

        final List<RegisteredPlugin> plugins = PluginManager.getInstance().getPlugins(IPlugin.class);
        assertAll(
                () -> assertNotNull(plugins),
                () -> assertEquals(1, plugins.size())
        );
    }

    @Test
    void pluginIsDeployedBecauseMostRecent() throws IOException {
        final String pluginTimestamp = System.currentTimeMillis() + "";

        final File version1_0 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.0");
        final File version1_2 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.2");

        PluginManager.getInstance().installPlugin(version1_0);
        PluginManager.getInstance().installPlugin(version1_2);

        final List<RegisteredPlugin> plugins = PluginManager.getInstance().getPlugins(IPlugin.class);
        assertAll(
                () -> assertNotNull(plugins),
                () -> assertEquals(1, plugins.size()),
                () -> assertEquals("1.2", plugins.get(0).getVersion())
        );
    }

    @Test
    void pluginIsNotDeployedBecauseTooOld() throws IOException {
        final String pluginTimestamp = System.currentTimeMillis() + "";

        final File version1_0 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.0");
        final File version1_2 = createDummyPlugin(pluginTimestamp, pluginTimestamp, "1.2");

        PluginManager.getInstance().installPlugin(version1_2);
        PluginManager.getInstance().installPlugin(version1_0);

        final List<RegisteredPlugin> plugins = PluginManager.getInstance().getPlugins(IPlugin.class);
        assertAll(
                () -> assertNotNull(plugins),
                () -> assertEquals(1, plugins.size()),
                () -> assertEquals("1.2", plugins.get(0).getVersion())
        );
    }

    @Test
    void uninstallPlugin() throws IOException {
        final File plugin01 = createDummyPlugin("plugin-01", "Plugin 01", "1.0");
        final File plugin02 = createDummyPlugin("plugin-02", "Plugin 02", "1.0");

        PluginManager.getInstance().installPlugin(plugin02);
        PluginManager.getInstance().installPlugin(plugin01);
        PluginManager.getInstance().uninstallPlugin(new PluginFile(plugin01));

        final List<RegisteredPlugin> plugins = PluginManager.getInstance().getPlugins(IPlugin.class);
        assertAll(
                () -> assertNotNull(plugins),
                () -> assertEquals(1, plugins.size()),
                () -> assertEquals("1.0", plugins.get(0).getVersion()),
                () -> assertEquals("Plugin 02", plugins.get(0).getName())
        );
    }
}
