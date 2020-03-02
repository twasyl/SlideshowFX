module slideshowfx.textile {
    exports com.twasyl.slideshowfx.markup.textile;

    provides com.twasyl.slideshowfx.markup.IMarkup with com.twasyl.slideshowfx.markup.textile.TextileMarkup;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.markup.textile.TextileMarkup;

    requires java.logging;
    requires org.eclipse.mylyn.wikitext;
    requires org.eclipse.mylyn.wikitext.textile;
    requires slideshowfx.markup;
}