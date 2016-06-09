package com.twasyl.slideshowfx.hosting.connector.dropbox.activator;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.dropbox.DropboxHostingConnector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator class for the connector that allows to interact with Dropbox.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class DropboxHostingConnectorActivator implements BundleActivator {
    private DropboxHostingConnector hostingConnector;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.hostingConnector = new DropboxHostingConnector();
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IHostingConnector.class.getName(), this.hostingConnector, props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if(this.hostingConnector.isAuthenticated()) this.hostingConnector.disconnect();
    }
}
