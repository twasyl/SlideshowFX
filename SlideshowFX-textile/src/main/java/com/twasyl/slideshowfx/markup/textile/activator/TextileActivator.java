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
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IMarkup.class.getName(), new TextileMarkup(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
