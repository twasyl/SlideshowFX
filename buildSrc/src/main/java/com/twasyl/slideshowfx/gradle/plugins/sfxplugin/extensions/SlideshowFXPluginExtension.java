package com.twasyl.slideshowfx.gradle.plugins.sfxplugin.extensions;

import org.gradle.api.Action;
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
    public SlideshowFXBundleExtension bundle;
    public boolean contentExtension = false;
    public boolean hostingConnector = false;
    public boolean snippetExecutor = false;
    public boolean markupPlugin = false;

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

    public String getBundlePackageDestination() {
        String location = "plugins" + File.separator;

        if (isMarkupPlugin()) {
            location += "markups";
        } else if (isSnippetExecutor()) {
            location += "executors";
        } else if (isHostingConnector()) {
            location += "hostingConnectors";
        } else if (isContentExtension()) {
            location += "extensions";
        } else {
            return null;
        }

        return location;
    }
}