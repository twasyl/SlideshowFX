package com.twasyl.slideshowfx.Jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;

/**
 * @author Thierry Wasylczenko
 */
public class JsoupTest {

    @Test
    public void parseHtmlMarkup() {
        Element element = Jsoup.parse("<div>Test</div>").body().child(0);


        System.out.println(element);
    }
}
