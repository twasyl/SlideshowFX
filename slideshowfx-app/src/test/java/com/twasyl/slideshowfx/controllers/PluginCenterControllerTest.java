package com.twasyl.slideshowfx.controllers;

import com.twasyl.slideshowfx.plugin.manager.internal.PluginFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Class testing the {@link PluginCenterController} class.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.1
 */
public class PluginCenterControllerTest {

    private static File FILE_LOCATION;
    private static PluginFile MISSING_FILE;
    private static File EMPTY_FILE;
    private static File NO_BUNDLE_NAME_FILE;
    private static File NO_BUNDLE_VERSION_FILE;
    private static File NO_BUNDLE_ACTIVATOR_FILE;
    private static File CORRECT_FILE;

    private static final PluginCenterController controller = new PluginCenterController();

    @BeforeAll
    public static void setUp() throws URISyntaxException, IOException {
        FILE_LOCATION = new File(PluginCenterControllerTest.class.getResource("/com/twasyl/slideshowfx/files/plugincenter").toURI());
        MISSING_FILE = new PluginFile(new File(FILE_LOCATION, "missing-plugin.sfx-plugin"));
        EMPTY_FILE = new File(FILE_LOCATION, "empty.txt");
        NO_BUNDLE_NAME_FILE = new File(FILE_LOCATION, "no-bundle-name.jar");
        NO_BUNDLE_VERSION_FILE = new File(FILE_LOCATION, "no-bundle-version.jar");
        NO_BUNDLE_ACTIVATOR_FILE = new File(FILE_LOCATION, "no-bundle-activator.jar");
        CORRECT_FILE = new File(FILE_LOCATION, "correct.jar");
    }

    @Test
    public void fileSeemsInvalidWhenNullFile() {
        assertThrows(NullPointerException.class, () -> controller.fileSeemsValid(null));
    }

    @Test
    public void fileSeemsInvalidWhenFileNotExists() {
        assertThrows(FileNotFoundException.class, () -> controller.fileSeemsValid(MISSING_FILE));
    }

    // TODO Transform to proper integration test using testfx
//    @Test
//    public void fileSeemsInvalidWhenNoBundleName() throws FileNotFoundException {
//        assertFalse(controller.fileSeemsValid(NO_BUNDLE_NAME_FILE));
//    }
//
//    @Test
//    public void fileSeemsInvalidWhenNoBundleVersion() throws FileNotFoundException {
//        assertFalse(controller.fileSeemsValid(NO_BUNDLE_VERSION_FILE));
//    }
//
//    @Test
//    public void fileSeemsInvalidWhenNoBundleActivator() throws FileNotFoundException {
//        assertFalse(controller.fileSeemsValid(NO_BUNDLE_ACTIVATOR_FILE));
//    }
//
//    @Test
//    public void fileSeemsValid() throws FileNotFoundException {
//        assertTrue(controller.fileSeemsValid(CORRECT_FILE));
//    }

    @Test
    public void isManifestAttributeInvalidWhenNull() {
        assertFalse(controller.isValueValid(null));
    }

    @Test
    public void isManifestAttributeInvalidWhenEmpty() {
        assertFalse(controller.isValueValid(""));
    }

    @Test
    public void isManifestAttributeInvalidWhenOnlySpaces() {
        assertFalse(controller.isValueValid("    "));
    }
}
