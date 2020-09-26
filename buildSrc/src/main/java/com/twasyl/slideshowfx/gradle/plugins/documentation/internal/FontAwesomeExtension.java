package com.twasyl.slideshowfx.gradle.plugins.documentation.internal;

import org.commonmark.Extension;
import org.commonmark.renderer.html.HtmlRenderer;

public class FontAwesomeExtension implements HtmlRenderer.HtmlRendererExtension {


    private FontAwesomeExtension() {
    }

    public static Extension create() {
        final FontAwesomeExtension fontAwesomeExtension = new FontAwesomeExtension();
        return fontAwesomeExtension;
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(FontAwesomeRenderer::new);
    }
}
