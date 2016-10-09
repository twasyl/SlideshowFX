package com.twasyl.slideshowfx.setup;

import com.twasyl.slideshowfx.setup.app.SlideshowFXSetup;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SlideshowFXSetupTest extends SlideshowFXSetup {

    public static void main(String[] args) {
        final String baseDir = "./build/distributions/SlideshowFX-NEXT-VERSION/SlideshowFXSetup.app/Contents/Java/package";

        System.setProperty("setup.plugins.directory", baseDir + "/plugins");
        System.setProperty("setup.documentations.directory", baseDir + "/documentations");
        System.setProperty("setup.application.artifact", baseDir + "SlideshowFX.app");
        System.setProperty("setup.application.name", "SlideshowFX");
        System.setProperty("setup.application.version", "1.0");
        launch(args);
    }
}
