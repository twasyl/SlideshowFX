/*
 * Copyright 2016 Thierry Wasylczenko
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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Performs tests for the Asciidoctor markup syntax.
 *
 * @author Thierry Wasylczenko
 */
public class AsciidoctorMarkupTest {

    private static AsciidoctorMarkup markup;

    @BeforeClass
    public static void setUp() {
        markup = new AsciidoctorMarkup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateWithNull() {
        markup.convertAsHtml(null);
    }

    @Test public void generateH1() {
        final String result = markup.convertAsHtml("= A title");

        assertEquals("<h1>A title</h1>", result);
    }

    @Test public void generateH2() {
        final String result = markup.convertAsHtml("== A title");
        System.out.println(result);
        assertEquals("<h2>A title</h2>", result);
    }

    @Test public void generateInlineCode() {
        final String result = markup.convertAsHtml("<code>public class Java { }</code>");

        assertEquals("<code>public class Java { }</code>", result);
    }

    @Test public void generateCodeBloc() {
        final String result = markup.convertAsHtml("[source,java]\n----\nfinal String s;\n----\n");
        System.out.println(result);
        assertEquals("<pre><code>final String s;</code></pre>", result);
    }

    @Test public void generateStrong() {
        final String result = markup.convertAsHtml("*Strong text*");

        assertEquals("<strong>Strong text</strong>", result);
    }

    @Test public void generateUnorderedList() {
        final String result = markup.convertAsHtml("<ul><li>One</li><li>Two</li></ul>");

        assertEquals("<ul><li>One</li><li>Two</li></ul>", result);
    }
}
