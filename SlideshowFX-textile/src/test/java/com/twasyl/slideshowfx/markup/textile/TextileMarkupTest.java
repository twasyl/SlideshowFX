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

package com.twasyl.slideshowfx.markup.textile;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thierry Wasylczenko
 */
public class TextileMarkupTest {

    private static TextileMarkup markup;

    @BeforeClass
    public static void setUp() {
        markup = new TextileMarkup();
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateWithNull() {
        markup.convertAsHtml(null);
    }

    @Test public void generateH1() {
        final String result = markup.convertAsHtml("# A title");

        assertEquals("<h1>A title</h1>", result);
    }

    @Test public void generateH2() {
        final String result = markup.convertAsHtml("## A title");

        assertEquals("<h2>A title</h2>", result);
    }

    @Test public void generateInlineCode() {
        final String result = markup.convertAsHtml("`public class Java { }`");

        assertEquals("<p><code>public class Java { }</code></p>", result);
    }

    @Test public void generateCodeBloc() {
        final String result = markup.convertAsHtml("    final String s;");

        assertEquals("<pre><code>final String s;\n</code></pre>", result);
    }

    @Test public void generateStrong() {
        final String result = markup.convertAsHtml("*Strong text*");

        assertEquals("<p><em>Strong text</em></p>", result);
    }

    @Test public void generateUnorderedList() {
        final String result = markup.convertAsHtml("* One\n* Two");

        assertEquals("<ul>\n<li>One</li>\n<li>Two</li>\n</ul>", result);
    }
}
