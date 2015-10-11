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

package com.twasyl.slideshowfx.markup.textile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thierry Wasylczenko
 */
public class TextileMarkupTest {

    private static TextileMarkup markup;

    @BeforeClass
    public static void setUp() {
        markup = new TextileMarkup();
    }

    /**
     * Try to convert a null String in HTML. The except result is to catch an {@link java.lang.IllegalArgumentException}
     * otherwise the test is considered as failed.
     */
    @Test
    public void tryWithNullString() {
        try {
            markup.convertAsHtml(null);
            fail("When parsing a null String doesn't throw an IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            // Great
        }
    }

    /**
     * Test if the parsing of a h1. gives an expected result.
     */
    @Test public void testWithH1() {
        final String textile = "h1. Test my textile";

        try {
            final String result = markup.convertAsHtml(textile);
            Element h1Result = Jsoup.parse(result).body().child(0);

            assertNotNull(h1Result);
            assertEquals("The generated element is not an H1 markup", "h1", h1Result.tagName());
            assertTrue("The H1 markup doesn't contain text", h1Result.hasText());
            assertEquals("Test my textile", h1Result.html());
        } catch(IllegalArgumentException e) {
            fail("An IllegalArgumentException has been thrown");
        }
    }
}
