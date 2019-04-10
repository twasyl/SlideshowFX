package com.twasyl.slideshowfx.global.configuration;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.twasyl.slideshowfx.global.configuration.ContextFileWorker.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 2.0
 */
public class ContextFileWorkerTest {

    protected Node createIdNode(final String id) {
        final Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(ID_TAG);
        when(node.getTextContent()).thenReturn(id);

        return node;
    }

    protected Node createFileNode(final String path) {
        final Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(FILE_TAG);
        when(node.getTextContent()).thenReturn(path);

        return node;
    }

    protected Node createOpenedDateTimeNode(final LocalDateTime openedDateTime) {
        final Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(OPENED_DATE_TIME_TAG);
        when(node.getTextContent()).thenReturn(openedDateTime.toString());

        return node;
    }

    protected Node createRecentPresentationNode(final Node... children) {
        final Node node = mock(Node.class);
        when(node.getNodeName()).thenReturn(RECENT_PRESENTATION_TAG);

        final NodeList list = mock(NodeList.class);
        when(list.getLength()).thenReturn(children.length);
        when(list.item(anyInt())).then(invocation -> children[(int) invocation.getArgument(0)]);

        when(node.getChildNodes()).thenReturn(list);

        return node;
    }

    protected Document createDocumentFromString(final String xml) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        return document;
    }

    protected String createXmlStringFromRecentPresentations(final RecentPresentation... recentPresentations) {
        final StringBuilder xml = new StringBuilder("<slideshowfx><recentPresentations>");

        for (RecentPresentation recentPresentation : recentPresentations) {
            xml.append("<recentPresentation>")
                    .append("<id>").append(recentPresentation.getId()).append("</id>")
                    .append("<file>").append(recentPresentation.getNormalizedPath()).append("</file>");

            if (recentPresentation.getOpenedDateTime() != null) {
                xml.append("<openedDateTime>").append(recentPresentation.getOpenedDateTime()).append("</openedDateTime>");
            }

            xml.append("</recentPresentation>");
        }

        xml.append("</recentPresentations></slideshowfx>");

        return xml.toString();
    }

    protected void assertRecentPresentationNode(final Node createdNode, final RecentPresentation recentPresentation) throws XPathExpressionException {
        assertNotNull(createdNode);
        assertEquals(RECENT_PRESENTATION_TAG, createdNode.getNodeName());
        assertTrue(createdNode.hasChildNodes());

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        final Node idNode = (Node) xPath.evaluate("./" + ID_TAG, createdNode, NODE);
        assertNotNull(idNode);
        assertEquals(recentPresentation.getId(), idNode.getTextContent());

        final Node fileNode = (Node) xPath.evaluate("./" + FILE_TAG, createdNode, NODE);
        assertNotNull(fileNode);
        assertEquals(recentPresentation.getNormalizedPath(), fileNode.getTextContent());

        final Node openedDateTimeNode = (Node) xPath.evaluate("./" + OPENED_DATE_TIME_TAG, createdNode, NODE);
        assertNotNull(openedDateTimeNode);
        assertEquals(recentPresentation.getOpenedDateTime().toString(), openedDateTimeNode.getTextContent());
    }

    @Test
    public void createRecentPresentationFromNode() {
        final LocalDateTime date = LocalDateTime.now();
        final Path path = Paths.get("presentation.sfx");
        final String normalizedPath = path.toAbsolutePath().toString().replaceAll("\\\\", "/");
        final String id = Base64.getEncoder().encodeToString(normalizedPath.getBytes(UTF_8));

        final Node node = createRecentPresentationNode(createIdNode(id), createFileNode(path.toAbsolutePath().toString()), createOpenedDateTimeNode(date));
        final RecentPresentation recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(node);

        assertNotNull(recentPresentation);
        assertEquals(normalizedPath, recentPresentation.getNormalizedPath());
        assertEquals(date, recentPresentation.getOpenedDateTime());
    }

    @Test
    public void createRecentPresentationFromNodeWithoutPath() {
        final LocalDateTime date = LocalDateTime.now();

        final Node node = createRecentPresentationNode(createOpenedDateTimeNode(date));
        final RecentPresentation recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(node);

        assertNull(recentPresentation);
    }

    @Test
    public void createRecentPresentationFromNodeWithoutOpenedDate() {
        final String path = "/presentation.sfx";

        final Node node = createRecentPresentationNode(createFileNode(path));
        final RecentPresentation recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(node);

        assertNull(recentPresentation);
    }

    @Test
    public void createRecentPresentationFromNodeWithoutChildren() {
        final Node node = createRecentPresentationNode();
        final RecentPresentation recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(node);

        assertNull(recentPresentation);
    }

    @Test
    public void getRecentPresentationsNode() throws Exception {
        final String xml = "<slideshowfx><recentPresentations></recentPresentations></slideshowfx>";
        final Document document = createDocumentFromString(xml);

        final Node recentPresentationsNode = ContextFileWorker.getRecentPresentationsNode(document);

        assertNotNull(recentPresentationsNode);
        assertEquals(RECENT_PRESENTATIONS_TAG, recentPresentationsNode.getNodeName());
    }

    @Test
    public void tryToGetRecentPresentationsNodeWhenMissing() throws Exception {
        final String xml = "<slideshowfx></slideshowfx>";
        final Document document = createDocumentFromString(xml);

        final Node recentPresentationsNode = ContextFileWorker.getRecentPresentationsNode(document);

        assertNotNull(recentPresentationsNode);
        assertEquals(RECENT_PRESENTATIONS_TAG, recentPresentationsNode.getNodeName());
    }

    @Test
    public void tryToGetRecentPresentationNodesWhenMissing() throws Exception {
        final String xml = "<slideshowfx><recentPresentations></recentPresentations></slideshowfx>";
        final Document document = createDocumentFromString(xml);

        final Node recentPresentationsNode = ContextFileWorker.getRecentPresentationsNode(document);
        final List<Node> recentPresentationNodes = ContextFileWorker.getRecentPresentationNodes(recentPresentationsNode);

        assertNotNull(recentPresentationNodes);
        assertEquals(0, recentPresentationNodes.size());
    }

    @Test
    public void getRecentPresentations() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));

        final Document document = createDocumentFromString(this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2));
        final List<Node> recentPresentationNodes = ContextFileWorker.getRecentPresentationNodes(ContextFileWorker.getRecentPresentationsNode(document));

        assertNotNull(recentPresentationNodes);
        assertEquals(2, recentPresentationNodes.size());
    }

    @Test
    public void recentPresentationsCorrectlyCreated() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));

        final Document document = createDocumentFromString(this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2));
        final List<Node> recentPresentationNodes = ContextFileWorker.getRecentPresentationNodes(ContextFileWorker.getRecentPresentationsNode(document));

        RecentPresentation recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(recentPresentationNodes.get(0));
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation1.getNormalizedPath(), recentPresentation.getNormalizedPath());
        assertEquals(recentPresentation1.getOpenedDateTime(), recentPresentation.getOpenedDateTime());
        assertEquals(recentPresentation1.getId(), recentPresentation.getId());

        recentPresentation = ContextFileWorker.buildRecentPresentationFromNode(recentPresentationNodes.get(1));
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation2.getNormalizedPath(), recentPresentation.getNormalizedPath());
        assertEquals(recentPresentation2.getOpenedDateTime(), recentPresentation.getOpenedDateTime());
        assertEquals(recentPresentation2.getId(), recentPresentation.getId());
    }

    @Test
    public void recentPresentationsAreSortedByNormalizedPath() {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation3 = new RecentPresentation("presentation3.sfx", recentPresentation2.getOpenedDateTime().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation3, recentPresentation1, recentPresentation2);
        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
        final Set<RecentPresentation> presentations = ContextFileWorker.readRecentPresentationFromStream(input);

        final Iterator<RecentPresentation> iterator = presentations.iterator();
        RecentPresentation recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation1.getNormalizedPath(), recentPresentation.getNormalizedPath());
        assertEquals(recentPresentation1.getOpenedDateTime(), recentPresentation.getOpenedDateTime());
        assertEquals(recentPresentation1.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation2.getNormalizedPath(), recentPresentation.getNormalizedPath());
        assertEquals(recentPresentation2.getOpenedDateTime(), recentPresentation.getOpenedDateTime());
        assertEquals(recentPresentation2.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation3.getNormalizedPath(), recentPresentation.getNormalizedPath());
        assertEquals(recentPresentation3.getOpenedDateTime(), recentPresentation.getOpenedDateTime());
        assertEquals(recentPresentation3.getId(), recentPresentation.getId());
    }

    @Test
    public void saveWithEmptyDocument() throws Exception {
        final String path = "presentation.sfx";
        final LocalDateTime openedDateTime = LocalDateTime.now();

        final RecentPresentation recentPresentation = new RecentPresentation(path, openedDateTime);
        final ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        ContextFileWorker.saveRecentPresentation(input, output, recentPresentation);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new ByteArrayInputStream(output.toByteArray()));

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        final String baseExpr = new StringBuilder("/").append(ROOT_TAG).append("/").append(RECENT_PRESENTATIONS_TAG).append("/").append(RECENT_PRESENTATION_TAG).append("[1]").toString();
        final String actualFile = (String) xPath.evaluate(baseExpr + "/" + FILE_TAG, document.getDocumentElement(), STRING);
        final String actualId = (String) xPath.evaluate(baseExpr + "/" + ID_TAG, document.getDocumentElement(), STRING);

        assertNotNull(actualId);
        assertEquals(recentPresentation.getId(), actualId);

        assertNotNull(actualFile);
        assertEquals(recentPresentation.getNormalizedPath(), actualFile);

        final LocalDateTime actualOpenedDateTime = LocalDateTime.parse((String) xPath.evaluate(baseExpr + "/" + OPENED_DATE_TIME_TAG, document.getDocumentElement(), STRING));
        assertEquals(openedDateTime, actualOpenedDateTime);
    }

    @Test
    public void saveWithExistingDocument() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", LocalDateTime.now().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        ContextFileWorker.saveRecentPresentation(input, output, recentPresentation2);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(new ByteArrayInputStream(output.toByteArray()));

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        final String baseExpr = new StringBuilder("/").append(ROOT_TAG).append("/").append(RECENT_PRESENTATIONS_TAG).append("/").append(RECENT_PRESENTATION_TAG).append("[2]").toString();
        final String actualFile = (String) xPath.evaluate(baseExpr + "/" + FILE_TAG, document.getDocumentElement(), STRING);
        final String actualId = (String) xPath.evaluate(baseExpr + "/" + ID_TAG, document.getDocumentElement(), STRING);

        assertNotNull(actualId);
        assertEquals(recentPresentation2.getId(), actualId);

        assertNotNull(actualFile);
        assertEquals(recentPresentation2.getNormalizedPath(), actualFile);

        final LocalDateTime actualOpenedDateTime = LocalDateTime.parse((String) xPath.evaluate(baseExpr + "/" + OPENED_DATE_TIME_TAG, document.getDocumentElement(), STRING));
        assertEquals(recentPresentation2.getOpenedDateTime(), actualOpenedDateTime);
    }

    @Test
    public void saveWithoutOpenedPresentationDate() throws Exception {
        final String path = "presentation.sfx";

        final RecentPresentation recentPresentation = new RecentPresentation(path, null);
        final ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        ContextFileWorker.saveRecentPresentation(input, output, recentPresentation);

        assertEquals(0, output.size());
    }

    @Test
    public void readWithoutOpenedPresentationDate() {
        final ByteArrayInputStream input = new ByteArrayInputStream("<slideshowfx><recentPresentations><recentPresentation><id>presentation01</id><file>/presentation01.sfx</file></recentPresentation></recentPresentations></slideshowfx>".getBytes());

        final Set<RecentPresentation> recentPresentations = readRecentPresentationFromStream(input);

        assertNotNull(recentPresentations);
        assertEquals(0, recentPresentations.size());
    }

    @Test
    public void readWithoutPath() {
        final ByteArrayInputStream input = new ByteArrayInputStream(("<slideshowfx><recentPresentations><recentPresentation><id>presentation01</id><openedDateTime>" + LocalDateTime.now() + "</openedDateTime></recentPresentation></recentPresentations></slideshowfx>").getBytes());

        final Set<RecentPresentation> recentPresentations = readRecentPresentationFromStream(input);

        assertNotNull(recentPresentations);
        assertEquals(0, recentPresentations.size());
    }

    @Test
    public void populateEmptyDocument() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.newDocument();

        ContextFileWorker.populateDocumentIfNecessary(document);

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        assertNotNull(document.getDocumentElement());
        assertEquals(ROOT_TAG, document.getDocumentElement().getTagName());

        final Node recentPresentationsNode = (Node) xPath.evaluate("/" + ROOT_TAG + "/" + RECENT_PRESENTATIONS_TAG, document.getDocumentElement(), NODE);
        assertNotNull(recentPresentationsNode);
    }

    @Test
    public void populateDocumentWhenNoRecentPresentationsTag() throws Exception {
        final String xml = "<slideshowfx></slideshowfx>";
        final Document document = createDocumentFromString(xml);

        ContextFileWorker.populateDocumentIfNecessary(document);

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();

        final Node recentPresentationsNode = (Node) xPath.evaluate("/" + ROOT_TAG + "/" + RECENT_PRESENTATIONS_TAG, document.getDocumentElement(), NODE);
        assertNotNull(recentPresentationsNode);
    }

    @Test
    public void createNodeFromRecentPresentation() throws Exception {
        final String xml = "<slideshowfx></slideshowfx>";
        final Document document = createDocumentFromString(xml);

        final String path = "presentation.sfx";
        final LocalDateTime openedDateTime = LocalDateTime.now();

        final RecentPresentation recentPresentation = new RecentPresentation(path, openedDateTime);

        final Node createdNode = ContextFileWorker.createNodeFromRecentPresentation(document, recentPresentation);
        assertRecentPresentationNode(createdNode, recentPresentation);
    }

    @Test
    public void findExistingRecentPresentation() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2);
        final Document document = createDocumentFromString(xml);

        Node node = ContextFileWorker.findRecentPresentationNodeFromID(document, recentPresentation1);
        assertRecentPresentationNode(node, recentPresentation1);

        node = ContextFileWorker.findRecentPresentationNodeFromID(document, recentPresentation2);
        assertRecentPresentationNode(node, recentPresentation2);
    }

    @Test
    public void findRecentPresentationWhenMissing() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation3 = new RecentPresentation("presentation3.sfx", recentPresentation2.getOpenedDateTime().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2);
        final Document document = createDocumentFromString(xml);

        Node node = ContextFileWorker.findRecentPresentationNodeFromID(document, recentPresentation3);
        assertNull(node);
    }

    @Test
    public void updatePresentation() throws Exception {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", LocalDateTime.now().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        recentPresentation2.setOpenedDateTime(recentPresentation2.getOpenedDateTime().plusDays(2));
        ContextFileWorker.updateRecentPresentation(input, output, recentPresentation2);

        final Document updatedDocument = createDocumentFromString(new String(output.toByteArray(), UTF_8));
        final List<Node> nodes = ContextFileWorker.getRecentPresentationNodes(ContextFileWorker.getRecentPresentationsNode(updatedDocument));

        assertEquals(2, nodes.size());

        Node recentPresentation = ContextFileWorker.findRecentPresentationNodeFromID(updatedDocument, recentPresentation1);
        assertRecentPresentationNode(recentPresentation, recentPresentation1);

        recentPresentation = ContextFileWorker.findRecentPresentationNodeFromID(updatedDocument, recentPresentation2);
        assertRecentPresentationNode(recentPresentation, recentPresentation2);
    }

    @Test
    public void testRecentPresentationAlreadyPresent() {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", LocalDateTime.now().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));

        assertTrue(ContextFileWorker.recentPresentationAlreadyPresent(input, recentPresentation2));
    }

    @Test
    public void testRecentPresentationAlreadyPresentWhenNot() {
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", LocalDateTime.now().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));

        assertFalse(ContextFileWorker.recentPresentationAlreadyPresent(input, recentPresentation2));
    }

    @Test
    public void purgeRecentPresentations() throws ContextFileException {
        final int numberOfRecentPresentationsToKeep = 2;
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation3 = new RecentPresentation("presentation3.sfx", recentPresentation2.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation4 = new RecentPresentation("presentation4.sfx", null);

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2, recentPresentation3, recentPresentation4);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Set<RecentPresentation> presentations = ContextFileWorker.purgeRecentPresentations(input, output, numberOfRecentPresentationsToKeep);
        assertEquals(numberOfRecentPresentationsToKeep, presentations.size());

        final Iterator<RecentPresentation> iterator = presentations.iterator();
        RecentPresentation recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation2.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation3.getId(), recentPresentation.getId());
    }

    @Test
    public void purgeRecentPresentationsWhenEqualAsSpecified() throws ContextFileException {
        final int numberOfRecentPresentationsToKeep = 3;
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation3 = new RecentPresentation("presentation3.sfx", recentPresentation2.getOpenedDateTime().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2, recentPresentation3);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Set<RecentPresentation> presentations = ContextFileWorker.purgeRecentPresentations(input, output, numberOfRecentPresentationsToKeep);
        assertEquals(numberOfRecentPresentationsToKeep, presentations.size());

        final Iterator<RecentPresentation> iterator = presentations.iterator();
        RecentPresentation recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation1.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation2.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation3.getId(), recentPresentation.getId());
    }

    @Test
    public void purgeRecentPresentationsWhenLessThanSpecified() throws ContextFileException {
        final int numberOfRecentPresentationsToKeep = 4;
        final RecentPresentation recentPresentation1 = new RecentPresentation("presentation1.sfx", LocalDateTime.now());
        final RecentPresentation recentPresentation2 = new RecentPresentation("presentation2.sfx", recentPresentation1.getOpenedDateTime().plusDays(2));
        final RecentPresentation recentPresentation3 = new RecentPresentation("presentation3.sfx", recentPresentation2.getOpenedDateTime().plusDays(2));

        final String xml = this.createXmlStringFromRecentPresentations(recentPresentation1, recentPresentation2, recentPresentation3);

        final ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(UTF_8));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Set<RecentPresentation> presentations = ContextFileWorker.purgeRecentPresentations(input, output, numberOfRecentPresentationsToKeep);
        assertEquals(3, presentations.size());

        final Iterator<RecentPresentation> iterator = presentations.iterator();
        RecentPresentation recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation1.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation2.getId(), recentPresentation.getId());

        recentPresentation = iterator.next();
        assertNotNull(recentPresentation);
        assertEquals(recentPresentation3.getId(), recentPresentation.getId());
    }
}
