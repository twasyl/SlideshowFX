package com.twasyl.slideshowfx.gradle.plugins.documentation.internal;

import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.Map;
import java.util.Set;

public class FontAwesomeRenderer implements NodeRenderer {

    private final HtmlWriter htmlWriter;
    private final CoreHtmlNodeRenderer coreHtmlNodeRenderer;

    public FontAwesomeRenderer(final HtmlNodeRendererContext context) {
        this.htmlWriter = context.getWriter();
        this.coreHtmlNodeRenderer = new CoreHtmlNodeRenderer(context);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(Image.class);
    }

    @Override
    public void render(Node node) {
        final var fontAwesomePrefix = "fa:";
        if (node instanceof Image && ((Image) node).getDestination().startsWith(fontAwesomePrefix)) {
            final var icons = ((Image) node).getDestination().substring(fontAwesomePrefix.length()).split("\\+");
            boolean isStack = icons.length > 1;
            boolean hasStackClassDefinition = false;

            if (isStack) {
                final var stackPrefix = "stack:";
                final String clazz;
                hasStackClassDefinition = icons[0].startsWith(stackPrefix);

                if (hasStackClassDefinition) {
                    clazz = icons[0].substring(stackPrefix.length()).replace(',', ' ');
                } else {
                    clazz = "fa-stack";
                }
                this.htmlWriter.tag("span", Map.of("class", clazz));
            }

            for (int index = (hasStackClassDefinition ? 1 : 0); index < icons.length; index++) {
                final var icon = icons[index];
                this.htmlWriter.tag("i", Map.of("class", icon.replace(',', ' ')));
                this.htmlWriter.tag("/i");
            }

            if (isStack) {
                this.htmlWriter.tag("/span");
            }
        } else {
            this.coreHtmlNodeRenderer.render(node);
        }
    }
}
