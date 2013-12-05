package com.twasyl.lat.utils;


import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DOMUtils {

    public static String convertNodeToText(Node node) {
        if(node == null) throw new IllegalArgumentException("Node can not be null");

        String result = null;

        final DOMSource source = new DOMSource(node);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Transformer transformer = null;

        try {
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
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
}
