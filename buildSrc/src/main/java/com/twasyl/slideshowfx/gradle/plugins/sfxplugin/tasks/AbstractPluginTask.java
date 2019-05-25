package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.tasks;

import com.twasyl.slideshowfx.gradle.plugins.sfxplugin.SlideshowFXPlugin;
import org.gradle.api.DefaultTask;

import java.io.File;

/**
 * Abstract task for creating tasks for the {@link SlideshowFXPlugin}.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class AbstractPluginTask extends DefaultTask {

    protected File sfxDir = new File(System.getProperty("user.home"), ".SlideshowFX");
    protected File pluginsDir = new File(sfxDir, "plugins");
}
