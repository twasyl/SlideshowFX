package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.plugin.InstalledPlugin;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

/**
 * Class testing the {@link PluginCenterController} class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class PluginCenterControllerTest {

    private static final String FILE_LOCATION = "./build/resources/test/com/twasyl/slideshowfx/files/plugincenter";
    private static final File MISSING_FILE = new File(FILE_LOCATION, "missing.txt");
    private static final File EMPTY_FILE = new File(FILE_LOCATION, "empty.txt");
    private static final File NO_BUNDLE_NAME_FILE = new File(FILE_LOCATION, "no-bundle-name.jar");
    private static final File NO_BUNDLE_VERSION_FILE = new File(FILE_LOCATION, "no-bundle-version.jar");
    private static final File NO_BUNDLE_ACTIVATOR_FILE = new File(FILE_LOCATION, "no-bundle-activator.jar");
    private static final File CORRECT_FILE = new File(FILE_LOCATION, "correct.jar");

    private static final PluginCenterController controller = new PluginCenterController();

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
    public void createInstalledPlugin() {
        final InstalledPlugin plugin = controller.createInstalledPlugin(CORRECT_FILE);

        assertEquals("Correct", plugin.getName());
        assertEquals("1.0", plugin.getVersion());
    }
}
