/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.Jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Thierry Wasylczenko
 */
@Ignore
public class JsoupTest {

    @Test
    public void parseHtmlMarkup() {
        Element element = Jsoup.parse("<div>Test</div>").body().child(0);


        System.out.println(element);
    }
}
