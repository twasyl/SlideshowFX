package com.twasyl.slideshowfx.markup.textile.activator;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.textile.TextileMarkup;
import org.apache.felix.framework.util.MapToDictionary;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * @author Thierry Wasylczenko
 */
public class TextileActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(IMarkup.class.getName(), new TextileMarkup(), new MapToDictionary(Collections.emptyMap()));
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // Nothing required when stopping this bundle
    }
}
