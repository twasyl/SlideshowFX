module slideshowfx.markdown {
    exports com.twasyl.slideshowfx.markup.markdown;

    provides com.twasyl.slideshowfx.markup.IMarkup with com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.markup.markdown.MarkdownMarkup;

    requires slideshowfx.markup;
    requires txtmark;
}