package com.twasyl.slideshowfx.plugin;

import com.twasyl.slideshowfx.osgi.OSGiManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class BasePluginIntegrationTest {

    static File buildDir = new File("build");
    static File libsDir = new File(buildDir, "libs");
    static File tmpDir = new File(buildDir, "tmp");
    static File plugingsDir = new File(tmpDir, "plugins");

    private static void copyPluginToTestLocation(final File plugin) {
        Path copiedPlugin = new File(plugingsDir, plugin.getName()).toPath();
        try {
            Files.copy(plugin.toPath(), copiedPlugin, REPLACE_EXISTING);
        } catch (IOException e) {
            Assertions.fail("Can not copy plugin", e);
        }
    }

    @BeforeAll
    protected static void setup() {
        System.setProperty("plugins.dir", plugingsDir.getAbsolutePath());

        if (!plugingsDir.exists()) {
            plugingsDir.mkdirs();
        }

        Arrays.stream(libsDir.listFiles((dir, name) -> name.endsWith(".jar"))).forEach(BasePluginIntegrationTest::copyPluginToTestLocation);
    }

    @BeforeEach
    void startOSGi() {
        OSGiManager.getInstance().startAndDeploy();
    }

    @AfterEach
    void stopOSGi() {
        OSGiManager.getInstance().stop();
    }

    /**
     * Asserts a plugin is installed in the OSGi environment. The method checks that the service is installed in the
     * OSGi environment and that it has the given name.
     *
     * @param pluginClass The class of the plugin: {@code IContentExtension.class}, {@code IHostingConnector.class}, {@code IMarkup.class}, {@code ISnippetExecutor.class}.
     * @param pluginName  The name of the plugin.
     */
    public void assertPluginIsInstalled(final Class<? extends IPlugin> pluginClass, final String pluginName) {
        final List<? extends IPlugin> installedServices = OSGiManager.getInstance().getInstalledServices(pluginClass);
        assertNotNull(installedServices);
        assertEquals(1, installedServices.size());
        assertEquals(pluginName, installedServices.get(0).getName());
    }
}
