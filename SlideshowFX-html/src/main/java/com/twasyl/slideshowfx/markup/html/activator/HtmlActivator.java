package com.twasyl.slideshowfx.markup.html.activator;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.html.HtmlMarkup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * @author Thierry Wasylczenko
 */
public class HtmlActivator implements BundleActivator {
    private static final Logger LOGGER = Logger.getLogger(HtmlActivator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IMarkup.class.getName(), new HtmlMarkup(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
