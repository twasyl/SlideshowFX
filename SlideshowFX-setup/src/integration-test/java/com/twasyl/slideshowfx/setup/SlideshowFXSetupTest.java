package com.twasyl.slideshowfx.setup;

import com.twasyl.slideshowfx.setup.app.SlideshowFXSetup;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SlideshowFXSetupTest extends SlideshowFXSetup {

    public static void main(String[] args) {
        System.setProperty("setup.plugins.directory", "./build/distributions/SlideshowFX-1.0/plugins");
        System.setProperty("setup.documentations.directory", "./build/distributions/SlideshowFX-1.0/documentations");
        System.setProperty("setup.application.artifact", "./build/distributions/SlideshowFX-1.0/SlideshowFX.app");
        System.setProperty("setup.application.name", "SlideshowFX");
        System.setProperty("setup.application.version", "1.0");
        launch(args);
    }
}
