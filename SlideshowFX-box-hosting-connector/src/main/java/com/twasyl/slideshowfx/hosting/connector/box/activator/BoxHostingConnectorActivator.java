package com.twasyl.slideshowfx.hosting.connector.box.activator;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.box.BoxHostingConnector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator class for the connector that allows to interact with Box.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class BoxHostingConnectorActivator implements BundleActivator {
    private BoxHostingConnector hostingConnector;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.hostingConnector = new BoxHostingConnector();
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IHostingConnector.class.getName(), this.hostingConnector, props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if(this.hostingConnector.isAuthenticated()) this.hostingConnector.disconnect();
    }
}
