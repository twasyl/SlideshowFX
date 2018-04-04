package com.twasyl.slideshowfx.content.extension.sequence.diagram.activator;

import com.twasyl.slideshowfx.content.extension.IContentExtension;
import com.twasyl.slideshowfx.content.extension.sequence.diagram.SequenceDiagramContentExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * The BundleActivator that register this OSGi module into the OSGi framework.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0
 */
public class SequenceDiagramContentExtensionActivator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        context.registerService(IContentExtension.class, new SequenceDiagramContentExtension(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
