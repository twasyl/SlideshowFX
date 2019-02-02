package com.twasyl.slideshowfx.markup.asciidoctor;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.jruby.javasupport.JavaEmbedUtils;

import java.util.Arrays;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * This class implements the asciidoctor syntax.
 * This markup language is identified byt the code {@code ASCIIDOCTOR} which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.2
 */
public class AsciidoctorMarkup extends AbstractMarkup {

    private static final Logger LOGGER = Logger.getLogger(AsciidoctorMarkup.class.getName());
    private final Asciidoctor asciidoctor;

    public AsciidoctorMarkup() {
        super("ASCIIDOCTOR", "asciidoctor", "ace/mode/asciidoc");

        this.asciidoctor = Asciidoctor.Factory.create(asList("uri:classloader:/gems/asciidoctor-1.5.8/lib"));
    }

    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        final AttributesBuilder attributes = AttributesBuilder.attributes()
                                                                .sectionNumbers(false)
                                                                .noFooter(true)
                                                                .tableOfContents(false)
                                                                .showTitle(false)
                                                                .skipFrontMatter(true)
                                                                .attribute("sectids!", "");
        final OptionsBuilder options = OptionsBuilder.options()
                                                     .compact(true)
                                                     .backend("html5")
                                                     .attributes(attributes);

        return this.asciidoctor.convert(markupString, options).trim();
    }
}
