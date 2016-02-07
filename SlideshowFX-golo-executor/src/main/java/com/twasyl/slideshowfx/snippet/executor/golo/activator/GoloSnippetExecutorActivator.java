package com.twasyl.slideshowfx.snippet.executor.golo.activator;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.golo.GoloSnippetExecutor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * The BundleActivator that register this OSGi module into the OSGi framework.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class GoloSnippetExecutorActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(ISnippetExecutor.class.getName(), new GoloSnippetExecutor(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
