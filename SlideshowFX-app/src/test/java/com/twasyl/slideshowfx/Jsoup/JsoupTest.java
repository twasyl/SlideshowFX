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
