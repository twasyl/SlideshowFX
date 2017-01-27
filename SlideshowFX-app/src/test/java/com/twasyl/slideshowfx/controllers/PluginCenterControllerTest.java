package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.plugin.InstalledPlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Class testing the {@link PluginCenterController} class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.1
 */
public class PluginCenterControllerTest {

    private static File FILE_LOCATION;
    private static File MISSING_FILE;
    private static File EMPTY_FILE;
    private static File NO_BUNDLE_NAME_FILE;
    private static File NO_BUNDLE_VERSION_FILE;
    private static File NO_BUNDLE_ACTIVATOR_FILE;
    private static File CORRECT_FILE;

    private static final PluginCenterController controller = new PluginCenterController();

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        FILE_LOCATION = new File(PluginCenterControllerTest.class.getResource("/com/twasyl/slideshowfx/files/plugincenter").toURI());
        MISSING_FILE = new File(FILE_LOCATION, "missing.txt");
        EMPTY_FILE = new File(FILE_LOCATION, "empty.txt");
        NO_BUNDLE_NAME_FILE = new File(FILE_LOCATION, "no-bundle-name.jar");
        NO_BUNDLE_VERSION_FILE = new File(FILE_LOCATION, "no-bundle-version.jar");
        NO_BUNDLE_ACTIVATOR_FILE = new File(FILE_LOCATION, "no-bundle-activator.jar");
        CORRECT_FILE = new File(FILE_LOCATION, "correct.jar");
    }

    @Test(expected = NullPointerException.class)
    public void fileSeemsInvalidWhenNullFile() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(null));
    }

    @Test(expected = FileNotFoundException.class)
    public void fileSeemsInvalidWhenFileNotExists() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(MISSING_FILE));
    }

    @Test
    public void fileSeemsInvalidWhenNotJar() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(EMPTY_FILE));
    }

    @Test
    public void fileSeemsInvalidWhenNoBundleName() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(NO_BUNDLE_NAME_FILE));
    }

    @Test
    public void fileSeemsInvalidWhenNoBundleVersion() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(NO_BUNDLE_VERSION_FILE));
    }

    @Test
    public void fileSeemsInvalidWhenNoBundleActivator() throws FileNotFoundException {
        assertFalse(controller.fileSeemsValid(NO_BUNDLE_ACTIVATOR_FILE));
    }

    @Test
    public void fileSeemsValid() throws FileNotFoundException {
        assertTrue(controller.fileSeemsValid(CORRECT_FILE));
    }

    @Test
    public void isManifestAttributeInvalidWhenNull() {
        assertFalse(controller.isManifestAttributeValid(null));
    }

    @Test
    public void isManifestAttributeInvalidWhenEmpty() {
        assertFalse(controller.isManifestAttributeValid(""));
    }

    @Test
    public void isManifestAttributeInvalidWhenOnlySpaces() {
        assertFalse(controller.isManifestAttributeValid("    "));
    }

    @Test
    public void createInstalledPlugin() throws URISyntaxException {
        final File file = new File(PluginCenterControllerTest.class.getResource("/com/twasyl/slideshowfx/files/plugincenter/correct.jar").toURI());
        System.out.println(file.getAbsolutePath());
        final InstalledPlugin plugin = controller.createInstalledPlugin(file);

        assertEquals("Correct", plugin.getName());
        assertEquals("1.0", plugin.getVersion());
    }
}
