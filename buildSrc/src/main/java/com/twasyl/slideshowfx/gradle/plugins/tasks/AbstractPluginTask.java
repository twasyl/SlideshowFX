package com.twasyl.slideshowfx.gradle.plugins.tasks;

import org.gradle.api.DefaultTask;

import java.io.File;

/**
 * Abstract task for creating tasks for the {@link com.twasyl.slideshowfx.gradle.plugins.SlideshowFXPlugin}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class AbstractPluginTask extends DefaultTask {

    protected File sfxDir = new File(System.getProperty("user.home"), ".SlideshowFX");
    protected File pluginsDir = new File(sfxDir, "plugins");
}
