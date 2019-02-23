package com.twasyl.slideshowfx.utils;


import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class DOMUtils {
    private static final Logger LOGGER = Logger.getLogger(DOMUtils.class.getName());

    private DOMUtils() {
    }

    public static String convertElementToText(Element element) {
        if (element == null) throw new IllegalArgumentException("Element can not be null");

        return element.outerHtml();
    }

    public static Element convertToNode(String text) {
        Element result = null;

        org.jsoup.nodes.Document document = Jsoup.parse(text);
        if (document != null) {
            result = document.body().child(0);
        }

        return result;
    }

    public static Document createDocument(String documentAsString) {
        Document document = null;

        document = Jsoup.parse(documentAsString);

        return document;
    }

    public static void saveDocument(Document document, File file) {
        document.outputSettings().prettyPrint(true);

        try (final Writer output = new DefaultCharsetWriter(file)) {
            output.write(document.outerHtml());
        } catch (IOException e) {
            LOGGER.log(SEVERE, "Can not save document", e);
        }
    }
}
