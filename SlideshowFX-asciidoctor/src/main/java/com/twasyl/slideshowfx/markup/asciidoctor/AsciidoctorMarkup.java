/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.markup.asciidoctor;

import com.twasyl.slideshowfx.markup.AbstractMarkup;
import org.asciidoctor.Asciidoctor;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class implements the asciidoctor syntax.
 * This markup language is identified byt the code <code>ASCIIDOCTOR</code> which is returned by {@link com.twasyl.slideshowfx.markup.IMarkup#getCode()}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
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

        JavaEmbedUtils.initialize(Arrays.asList("gems/asciidoctor-1.5.2/lib"), config);

        this.asciidoctor = Asciidoctor.Factory.create(AsciidoctorMarkup.class.getClassLoader());
    }

    @Override
    public String convertAsHtml(String markupString) throws IllegalArgumentException {
        if(markupString == null) throw new IllegalArgumentException("Can not convert " + getName() + " to HTML : the String is null");

        return this.asciidoctor.convert(markupString,new HashMap<String, Object>());
    }
}
