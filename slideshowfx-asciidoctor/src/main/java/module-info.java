module slideshowfx.asciidoctor {
    exports com.twasyl.slideshowfx.markup.asciidoctor;

    provides com.twasyl.slideshowfx.markup.IMarkup with com.twasyl.slideshowfx.markup.asciidoctor.AsciidoctorMarkup;
    provides com.twasyl.slideshowfx.plugin.IPlugin with com.twasyl.slideshowfx.markup.asciidoctor.AsciidoctorMarkup;

    requires asciidoctorj;
    requires asciidoctorj.api;
    requires java.logging;
    requires slideshowfx.markup;
}