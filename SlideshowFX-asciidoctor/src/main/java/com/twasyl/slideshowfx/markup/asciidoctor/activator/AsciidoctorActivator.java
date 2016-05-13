package com.twasyl.slideshowfx.markup.asciidoctor.activator;

import com.twasyl.slideshowfx.markup.IMarkup;
import com.twasyl.slideshowfx.markup.asciidoctor.AsciidoctorMarkup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * This class is the OSGi activator for the asciidoctor markup language.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class AsciidoctorActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();

        bundleContext.registerService(IMarkup.class.getName(), new AsciidoctorMarkup(), props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }
}
