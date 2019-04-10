package com.twasyl.slideshowfx.markup.markdown.activator;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * @author Thierry Wasylczenko
 */
public class MarkdownActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IMarkup.class.getName(), new MarkdownMarkup(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // Nothing to do when stopping the plugin
    }
}
