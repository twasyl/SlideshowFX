package com.twasyl.slideshowfx.utils;


import com.twasyl.slideshowfx.utils.io.DefaultCharsetWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

public class DOMUtils {

    public static String convertElementToText(Element element) {
        if(element == null) throw new IllegalArgumentException("Element can not be null");

        return element.outerHtml();
    }

    public static Element convertToNode(String text) {
        Element result = null;

        org.jsoup.nodes.Document document = Jsoup.parse(text);
        if(document != null) {
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
        String result = null;

        document.outputSettings().prettyPrint(true);

        try(final Writer output = new DefaultCharsetWriter(file)) {
            output.write(document.outerHtml());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
