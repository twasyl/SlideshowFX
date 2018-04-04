package com.twasyl.slideshowfx.markup.asciidoctor;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This class implements the asciidoctor syntax.
 * This markup language is identified byt the code {@code ASCIIDOCTOR} which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.1
 */
public class AsciidoctorMarkup extends AbstractMarkup {

    private static final Logger LOGGER = Logger.getLogger(AsciidoctorMarkup.class.getName());
    private final Asciidoctor asciidoctor;

    public AsciidoctorMarkup() {
        super("ASCIIDOCTOR", "asciidoctor", "ace/mode/asciidoc");

        /*
         This part is absolutely mandatory in order to be able to instantiate asciidoctor in an
         OSGi context. In someways it initialize Ruby for Java by getting/discovering the classpath.
         Without it, in the OSGi context it will be impossible to find JRuby and asciidoctor gems.
         */
        RubyInstanceConfig config = new RubyInstanceConfig();
        config.setLoader(AsciidoctorMarkup.class.getClassLoader());

        JavaEmbedUtils.initialize(Arrays.asList("gems/asciidoctor-1.5.6.1/lib"), config);

        this.asciidoctor = Asciidoctor.Factory.create(AsciidoctorMarkup.class.getClassLoader());
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
