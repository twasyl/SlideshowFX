module slideshowfx.html {
    exports com.twasyl.slideshowfx.markup.html;

    provides com.twasyl.slideshowfx.markup.IMarkup with com.twasyl.slideshowfx.markup.html.HtmlMarkup;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.markup.html.HtmlMarkup;

    requires java.logging;
    requires slideshowfx.markup;
}