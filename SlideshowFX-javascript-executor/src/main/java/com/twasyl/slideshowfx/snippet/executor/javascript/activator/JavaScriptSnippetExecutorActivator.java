package com.twasyl.slideshowfx.snippet.executor.javascript.activator;

import com.twasyl.slideshowfx.snippet.executor.ISnippetExecutor;
import com.twasyl.slideshowfx.snippet.executor.javascript.JavaScriptSnippetExecutor;
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
public class JavaScriptSnippetExecutorActivator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        context.registerService(ISnippetExecutor.class.getName(), new JavaScriptSnippetExecutor(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
