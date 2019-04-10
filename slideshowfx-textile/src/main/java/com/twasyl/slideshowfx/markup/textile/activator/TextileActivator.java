package com.twasyl.slideshowfx.markup.textile.activator;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * @author Thierry Wasylczenko
 */
public class TextileActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(IMarkup.class.getName(), new TextileMarkup(), new Hashtable<>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // Nothing to do when stopping the plugin
    }
}
