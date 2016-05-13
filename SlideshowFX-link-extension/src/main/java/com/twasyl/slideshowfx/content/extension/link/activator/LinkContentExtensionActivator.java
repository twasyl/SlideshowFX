package com.twasyl.slideshowfx.content.extension.link.activator;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.content.extension.link.LinkContentExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator class for the content extension that allows to insert links easily inside a SlideshowFX presentation.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class LinkContentExtensionActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IContentExtension.class.getName(), new LinkContentExtension(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
