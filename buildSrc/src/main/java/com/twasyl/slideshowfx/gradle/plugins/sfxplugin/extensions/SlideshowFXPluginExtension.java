package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

/**
 * Plugin extension for defining the properties of the plugin.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX @@NEXT-VERSION@@
 */
public class SlideshowFXPluginExtension {
    private SlideshowFXBundleExtension bundle;
    private boolean contentExtension = false;
    private boolean hostingConnector = false;
    private boolean snippetExecutor = false;
    private boolean markupPlugin = false;
    private String pluginClass;

    public SlideshowFXPluginExtension(ObjectFactory objectFactory) {
        bundle = objectFactory.newInstance(SlideshowFXBundleExtension.class);
    }

    public void bundle(Action<SlideshowFXBundleExtension> action) {
        action.execute(this.bundle);
    }

    public SlideshowFXBundleExtension getBundle() {
        return bundle;
    }

    public void setBundle(SlideshowFXBundleExtension bundle) {
        this.bundle = bundle;
    }

    public boolean isContentExtension() {
        return contentExtension;
    }

    public void setContentExtension(boolean contentExtension) {
        this.contentExtension = contentExtension;
    }

    public boolean isHostingConnector() {
        return hostingConnector;
    }

    public void setHostingConnector(boolean hostingConnector) {
        this.hostingConnector = hostingConnector;
    }

    public boolean isSnippetExecutor() {
        return snippetExecutor;
    }

    public void setSnippetExecutor(boolean snippetExecutor) {
        this.snippetExecutor = snippetExecutor;
    }

    public boolean isMarkupPlugin() {
        return markupPlugin;
    }

    public void setMarkupPlugin(boolean markupPlugin) {
        this.markupPlugin = markupPlugin;
    }

    public File getBundlePackageDestination(final Project bundleProject) {
        final File baseLocation = new File(bundleProject.getBuildDir(), "tmp/bundles/package/plugins");

        if (isMarkupPlugin()) {
            return new File(baseLocation, "markups");
        } else if (isSnippetExecutor()) {
            return new File(baseLocation, "executors");
        } else if (isHostingConnector()) {
            return new File(baseLocation, "hostingConnectors");
        } else if (isContentExtension()) {
            return new File(baseLocation, "extensions");
        } else {
            return null;
        }
    }
}