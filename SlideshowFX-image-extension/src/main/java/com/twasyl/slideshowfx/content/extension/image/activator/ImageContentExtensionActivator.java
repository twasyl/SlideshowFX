package com.twasyl.slideshowfx.content.extension.image.activator;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.content.extension.image.ImageContentExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator class for the content extension that allows to insert quote easily inside a SlideshowFX presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ImageContentExtensionActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IContentExtension.class.getName(), new ImageContentExtension(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
