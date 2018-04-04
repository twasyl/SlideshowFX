package com.twasyl.slideshowfx.osgi;

import com.twasyl.slideshowfx.utils.io.CopyFileVisitor;
import com.twasyl.slideshowfx.utils.io.IOUtils;
import org.junit.jupiter.api.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.osgi.framework.Constants.SYSTEM_BUNDLE_LOCATION;

/**
 * This class tests the {@link OSGiManager} classes
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class OSGiManagerTest {

    private static OSGiManager osgiManager;
    private static final File ROOT_OSGI = new File("build/resources/test/com/twasyl/slideshowfx/osgi");
    private static final File PLUGINS_DIR = new File(ROOT_OSGI, "testPlugins");
    private static final File OSGI_CACHE_DIR = new File(ROOT_OSGI, "felix-cache");
    private static final File PLUGIN_1_0 = new File(PLUGINS_DIR, "plugin-1.0.jar");
    private static final File PLUGIN_1_1 = new File(PLUGINS_DIR, "plugin-1.1.jar");
    private static final File PLUGIN_1_2 = new File(PLUGINS_DIR, "plugin-1.2.jar");
    private static final File PLUGIN2_3_0 = new File(PLUGINS_DIR, "plugin2-3.0.jar");

    @BeforeAll
    public static void setUp() {
        osgiManager = new OSGiManager();
        osgiManager.pluginsDirectory = PLUGINS_DIR;
    }

    @AfterAll
    public static void tearDown() throws IOException {
        IOUtils.deleteDirectory(OSGI_CACHE_DIR);
    }

    @BeforeEach
    public void before() throws URISyntaxException, IOException {
        final Path source = new File("src/test/resources/com/twasyl/slideshowfx/osgi/testPlugins").toPath().toAbsolutePath();
        Files.walkFileTree(source, new CopyFileVisitor(ROOT_OSGI.toPath().toAbsolutePath(), source));

        osgiManager.osgiCache = new File(OSGI_CACHE_DIR, System.currentTimeMillis() + "");
        osgiManager.start();
    }

    @AfterEach
    public void after() {
        osgiManager.stop();
    }

    @Test
    public void isMostRecent() throws BundleException {
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_0.getAbsolutePath()));

        assertTrue(osgiManager.isPluginMostRecent(bundle));
    }

    @Test
    public void isNotMostRecent() throws BundleException {
        osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_2.getAbsolutePath()));
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_0.getAbsolutePath()));

        assertFalse(osgiManager.isPluginMostRecent(bundle));
    }

    @Test
    public void isActive() throws BundleException {
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s",PLUGIN_1_0.getAbsolutePath()));
        bundle.start();

        assertTrue(osgiManager.isPluginActive(bundle));
    }

    @Test
    public void isNotActive() throws BundleException {
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_0.getAbsolutePath()));

        assertFalse(osgiManager.isPluginActive(bundle));
    }

    @Test
    public void otherVersionInstalled() throws BundleException {
        osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_0.getAbsolutePath()));
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_1.getAbsolutePath()));

        assertTrue(osgiManager.isPluginInAnotherVersionInstalled(bundle));
    }

    @Test
    public void noOtherVersionInstalled() throws BundleException {
        final Bundle bundle = osgiManager.osgiFramework.getBundleContext().installBundle(String.format("file:%1$s", PLUGIN_1_0.getAbsolutePath()));

        assertFalse(osgiManager.isPluginInAnotherVersionInstalled(bundle));
    }

    @Test
    public void pluginIsDeployed() throws IOException {
        osgiManager.deployBundle(PLUGIN_1_0);

        // Expected number of plugins is 2 because Felix is a bundle itself
        assertEquals(2, osgiManager.osgiFramework.getBundleContext().getBundles().length);
    }

    @Test
    public void pluginIsDeployedBecauseMostRecent() throws IOException {
        osgiManager.deployBundle(PLUGIN_1_0);
        osgiManager.deployBundle(PLUGIN_1_1);
        osgiManager.deployBundle(PLUGIN_1_2);

        // Expected number of plugins is 2 because Felix is a bundle itself
        final Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(2, bundles.length);
    }

    @Test
    public void oldVersionsAreUninstalled() throws IOException {
        osgiManager.deployBundle(PLUGIN_1_0);
        osgiManager.deployBundle(PLUGIN_1_1);
        osgiManager.deployBundle(PLUGIN_1_2);

        final Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        final Bundle installedBundle = Arrays.stream(bundles)
                .filter(bundle -> "com.twasyl.slideshowfx.plugin".equals(bundle.getSymbolicName()))
                .findFirst()
                .orElse(null);

        assertNotNull(installedBundle);
        assertEquals("1.2.0", installedBundle.getVersion().toString());
    }

    @Test
    public void pluginIsNotDeployedBecauseTooOld() throws IOException {
        osgiManager.deployBundle(PLUGIN_1_2);
        osgiManager.deployBundle(PLUGIN_1_0);
        osgiManager.deployBundle(PLUGIN_1_1);

        // Expected number of plugins is 2 because Felix is a bundle itself
        final Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(2, bundles.length);

        final Bundle installedBundle = Arrays.stream(bundles)
                .filter(bundle -> "com.twasyl.slideshowfx.plugin".equals(bundle.getSymbolicName()))
                .findFirst()
                .orElse(null);

        assertNotNull(installedBundle);
        assertEquals("1.2.0", installedBundle.getVersion().toString());
    }

    @Test
    public void undeployBundle() throws IOException, BundleException {
        osgiManager.deployBundle(PLUGIN_1_2);

        // Expected number of plugins is 2 because Felix is a bundle itself
        Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(2, bundles.length);

        osgiManager.uninstallBundle(PLUGIN_1_2);

        bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(1, bundles.length);
    }

    @Test
    public void undeployBundleWhenOtherDeployed() throws IOException, BundleException {
        osgiManager.deployBundle(PLUGIN2_3_0);
        osgiManager.deployBundle(PLUGIN_1_2);

        // Expected number of plugins is 3 because Felix is a bundle itself
        Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(3, bundles.length);

        osgiManager.uninstallBundle(PLUGIN_1_2);

        bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(2, bundles.length);

        final Bundle installedBundle = Arrays.stream(bundles)
                .filter(bundle -> "com.twasyl.slideshowfx.plugin2".equals(bundle.getSymbolicName()))
                .findFirst()
                .orElse(null);

        assertNotNull(installedBundle);
    }

    @Test
    public void startAndDeploy() {
        osgiManager.stop();
        osgiManager.startAndDeploy();
        assertNotNull(osgiManager.osgiFramework);

        // Expected number of plugins is 3 because Felix is a bundle itself
        Bundle[] bundles = osgiManager.osgiFramework.getBundleContext().getBundles();
        assertEquals(3, bundles.length);

        final long activePlugins = Arrays.stream(bundles)
                .filter(bundle -> !SYSTEM_BUNDLE_LOCATION.equals(bundle.getLocation()) && osgiManager.isPluginActive(bundle)).count();
        assertEquals(2, activePlugins);
    }
}
