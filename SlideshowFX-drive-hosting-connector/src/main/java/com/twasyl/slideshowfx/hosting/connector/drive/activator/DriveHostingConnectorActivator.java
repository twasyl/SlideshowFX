package com.twasyl.slideshowfx.hosting.connector.drive.activator;

import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.hosting.connector.drive.DriveHostingConnector;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * @author Thierry Wasylczenko
 */
public class DriveHostingConnectorActivator implements BundleActivator {
    private DriveHostingConnector hostingConnector;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        this.hostingConnector = new DriveHostingConnector();
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IHostingConnector.class.getName(), this.hostingConnector, props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if(this.hostingConnector.isAuthenticated()) this.hostingConnector.disconnect();
    }
}
