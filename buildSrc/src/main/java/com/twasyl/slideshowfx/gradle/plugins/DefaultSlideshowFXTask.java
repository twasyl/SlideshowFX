package com.twasyl.slideshowfx.gradle.plugins;

import org.gradle.api.DefaultTask;

/**
 * Default class for SlideshowFX gradle plugins. This class contains the extension the plugins use.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public abstract class DefaultSlideshowFXTask<T> extends DefaultTask {
    protected T extension;

    public DefaultSlideshowFXTask(Class<T> extension) {
        this.extension = getProject().getExtensions().getByType(extension);
    }
}
