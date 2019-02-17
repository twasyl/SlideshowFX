package com.twasyl.slideshowfx.plugin;

import com.twasyl.slideshowfx.osgi.OSGiManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
    static void setup() {
        System.setProperty("plugins.dir", plugingsDir.getAbsolutePath());

        if (!plugingsDir.exists()) {
            plugingsDir.mkdirs();
        }

        Arrays.stream(libsDir.listFiles((dir, name) -> name.endsWith(".jar"))).forEach(BasePluginIntegrationTest::copyPluginToTestLocation);

        OSGiManager.getInstance().startAndDeploy();
    }

    @AfterAll
    static void tearDown() {
        OSGiManager.getInstance().stop();
    }
}
