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

package com.twasyl.slideshowfx.utils;


import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class DOMUtils {

    public static String convertNodeToText(Node node) {
        if(node == null) throw new IllegalArgumentException("Node can not be null");

        String result = null;

        final DOMSource source = new DOMSource(node);

        Transformer transformer = null;

        try(final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            final StreamResult streamResult = new StreamResult(out);
            transformer.transform(source, streamResult);

            streamResult.getOutputStream().flush();

            result = new String(out.toByteArray());

            streamResult.getOutputStream().close();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Node convertToNode(String text) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Node node = builder.parse(
                new InputSource(
                        new ByteArrayInputStream(text.getBytes())
                )
        ).getDocumentElement();

        return node;
    }

    public static org.jsoup.nodes.Document createDocument(String documentAsString) {
        org.jsoup.nodes.Document document = null;

        document = Jsoup.parse(documentAsString);

        return document;
    }

    public static void saveDocument(org.jsoup.nodes.Document document, File file) {
        String result = null;

        document.outputSettings().prettyPrint(true);

        try(FileOutputStream output = new FileOutputStream(file)) {
            output.write(document.outerHtml().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };


    }
}
