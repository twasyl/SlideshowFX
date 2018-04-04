package com.twasyl.slideshowfx.global.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.xpath.XPathConstants.NODE;

/**
 * Class for interacting with a SlideshowFX context file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class ContextFileWorker {
    private static final Logger LOGGER = Logger.getLogger(ContextFileWorker.class.getName());
    private static DocumentBuilder DOCUMENT_BUILDER;
    private static XPath XPATH;

    static {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DOCUMENT_BUILDER = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Can not create a document builder instance", e);
        }

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        XPATH = xPathFactory.newXPath();
    }

    protected static final String ROOT_TAG = "slideshowfx";
    protected static final String RECENT_PRESENTATIONS_TAG = "recentPresentations";
    protected static final String RECENT_PRESENTATION_TAG = "recentPresentation";
    protected static final String ID_TAG = "id";
    protected static final String FILE_TAG = "file";
    protected static final String OPENED_DATE_TIME_TAG = "openedDateTime";

    /**
     * Determine from the given {@code contextFile} the recent presentations that have been opened by SlideshowFX.
     *
     * @param contextFile The context file to read.
     * @return A never {@code null} collection of the recent presentations opened by SlideshowFX.
     * @throws ContextFileException If the given {@code contextFile} is {@code null}.
     */
    public static Set<RecentPresentation> readRecentPresentationFromFile(final File contextFile) throws ContextFileException {
        if (contextFile == null) throw new NullPointerException("The context file can not be null");

        final InputStream input;
        try {
            input = getInputStreamForFile(contextFile);
        } catch (IOException e) {
            throw new ContextFileException(e);
        }

        return readRecentPresentationFromStream(input);
    }

    /**
     * Save a given {@code recentPresentation} to a specified {@code contextFile}. If the context file doesn't exist or
     * is empty, it's structure will be created in order to properly add the recent presentation to the file.
     *
     * @param contextFile        The context file to write to.
     * @param recentPresentation The presentation to add.
     * @throws ContextFileException
     */
    public static void saveRecentPresentationToFile(final File contextFile, final RecentPresentation recentPresentation) throws ContextFileException {
        if (contextFile == null) throw new NullPointerException("The context file can not be null");

        final InputStream input;
        final OutputStream output;
        try {
            input = getInputStreamForFile(contextFile);
            output = new FileOutputStream(contextFile);
        } catch (IOException e) {
            throw new ContextFileException(e);
        }

        saveRecentPresentation(input, output, recentPresentation);
    }

    /**
     * Update a given {@code recentPresentation} to a specified {@code contextFile}. If the context file doesn't exist or
     * is empty, it's structure will be created in order to properly add the recent presentation to the file.
     *
     * @param contextFile        The context file to write to.
     * @param recentPresentation The presentation to add.
     * @throws ContextFileException
     */
    public static void updateRecentPresentationInFile(final File contextFile, final RecentPresentation recentPresentation) throws ContextFileException {
        if (contextFile == null) throw new NullPointerException("The context file can not be null");

        final InputStream input;
        final OutputStream output;
        try {
            input = getInputStreamForFile(contextFile);
            output = new FileOutputStream(contextFile);
        } catch (IOException e) {
            throw new ContextFileException(e);
        }

        updateRecentPresentation(input, output, recentPresentation);
    }

    /**
     * Tests if a given {@link RecentPresentation recentPresentation} is found by it's ID in the given document.
     *
     * @param contextFile        The document to search in.
     * @param recentPresentation The presentation to search.
     * @return {@code true} if the presentation has been found in the document, {@code false} otherwise.
     */
    public static boolean recentPresentationAlreadyPresent(final File contextFile, final RecentPresentation recentPresentation) throws ContextFileException {
        if (contextFile == null) throw new NullPointerException("The context file can not be null");

        final InputStream input;
        try {
            input = getInputStreamForFile(contextFile);
        } catch (IOException e) {
            throw new ContextFileException(e);
        }

        return recentPresentationAlreadyPresent(input, recentPresentation);
    }

    /**
     * Purge the oldest recent presentations from the given document represented by {@code contextFile} in order to keep
     * at most the given number of recent presentations.
     *
     * @param contextFile                       The document to purge from.
     * @param numberOfRecentPresentationsToKeep The number of recent presentations to keep.
     * @return The recent presentations that are now stored in the context file.
     * @throws ContextFileException
     */
    public static Set<RecentPresentation> purgeRecentPresentations(final File contextFile, final long numberOfRecentPresentationsToKeep) throws ContextFileException {
        if (contextFile == null) throw new NullPointerException("The context file can not be null");
        if (numberOfRecentPresentationsToKeep < 0)
            throw new IllegalArgumentException("The number of recent presentations to keep can not be negative");

        final InputStream input;
        final OutputStream output;
        try {
            input = getInputStreamForFile(contextFile);
            output = new FileOutputStream(contextFile);
        } catch (IOException e) {
            throw new ContextFileException(e);
        }

        return purgeRecentPresentations(input, output, numberOfRecentPresentationsToKeep);
    }

    /**
     * <p>Save a given {@code recentPresentation} to a given {@code output}. The method will consider the {@code input}
     * object as the XML document and will try to read it and the result of the appending will be written in the {@code output}.</p>
     * <p>
     * If the input is considered empty (meaning {@link InputStream#available()} returns {@code 0}), then the structure
     * of XML file will be created in order to properly add the presentation.</p>
     *
     * @param input              The document to which append the presentation.
     * @param output             The document where the result of the appending will be persisted.
     * @param recentPresentation The presentation to add.
     * @throws ContextFileException
     */
    protected static void saveRecentPresentation(final InputStream input, final OutputStream output,
                                                 final RecentPresentation recentPresentation) throws ContextFileException {
        final Document document = createDocumentFromInput(input);

        populateDocumentIfNecessary(document);

        final Node recentPresentationsNode = getRecentPresentationsNode(document);
        final Node recentPresentationNode = createNodeFromRecentPresentation(document, recentPresentation);

        if (recentPresentationNode != null) {
            recentPresentationsNode.appendChild(recentPresentationNode);

            writeDocument(document, output);
        }
    }

    /**
     * <p>Update a given {@code recentPresentation} to a given {@code output}. The method will consider the {@code input}
     * object as the XML document and will try to read it and the result of the update will be written in the {@code output}.</p>
     * <p>If the input is considered empty (meaning {@link InputStream#available()} returns {@code 0}), then the structure
     * of XML file will be created in order to properly add the presentation.</p>
     *
     * @param input              The document to which update the presentation.
     * @param output             The document where the result of the update will be persisted.
     * @param recentPresentation The presentation to update.
     * @throws ContextFileException
     */
    protected static void updateRecentPresentation(final InputStream input, final OutputStream output,
                                                   final RecentPresentation recentPresentation) throws ContextFileException {
        final Document document = createDocumentFromInput(input);
        populateDocumentIfNecessary(document);

        Node recentPresentationNode = null;

        try {
            recentPresentationNode = findRecentPresentationNodeFromID(document, recentPresentation);
        } catch (ContextFileException e) {
            LOGGER.log(Level.WARNING, "Error when trying to find the recent presentation in document", e);
        }

        final Node recentPresentationsNode = getRecentPresentationsNode(document);
        final Node newRecentPresentationNode = createNodeFromRecentPresentation(document, recentPresentation);

        if (recentPresentationNode != null) {
            recentPresentationsNode.removeChild(recentPresentationNode);
        }

        recentPresentationsNode.appendChild(newRecentPresentationNode);

        writeDocument(document, output);
    }

    /**
     * Tests if a given {@link RecentPresentation recentPresentation} is found by it's ID in the given document.
     *
     * @param input              The document to search in.
     * @param recentPresentation The presentation to search.
     * @return {@code true} if the presentation has been found in the document, {@code false} otherwise.
     */
    protected static boolean recentPresentationAlreadyPresent(final InputStream input, final RecentPresentation recentPresentation) {
        boolean presentationFound = false;

        try {
            final Document document = DOCUMENT_BUILDER.parse(input);

            presentationFound = findRecentPresentationNodeFromID(document, recentPresentation) != null;
        } catch (ContextFileException | SAXException | IOException e) {
            LOGGER.log(Level.FINE, "Can not read XML document", e);
        }

        return presentationFound;
    }

    /**
     * Read all recent presentations from the provided document.
     *
     * @param input The document to read the recent presentations from.
     * @return A never {@code null} collection of the recent presentations stored within the document.
     */
    protected static Set<RecentPresentation> readRecentPresentationFromStream(final InputStream input) {
        final Set<RecentPresentation> presentations = new TreeSet<>();

        try {
            final Document document = createDocumentFromInput(input);

            final Node recentPresentationsNode = getRecentPresentationsNode(document);

            if (recentPresentationsNode != null && recentPresentationsNode.hasChildNodes()) {
                final List<Node> recentPresentationNodes = getRecentPresentationNodes(recentPresentationsNode);

                presentations.addAll(
                        recentPresentationNodes.stream()
                                .map(ContextFileWorker::buildRecentPresentationFromNode)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
            }
        } catch (ContextFileException e) {
            LOGGER.log(Level.SEVERE, "The context file can not be read", e);
        }

        return presentations;
    }

    /**
     * Purge the oldest recent presentations from the given document represented by {@code input} in order to keep at most
     * the given number of recent presentations.
     *
     * @param input                             The document to purge from.
     * @param output                            The document to write to.
     * @param numberOfRecentPresentationsToKeep The number of recent presentations to keep.
     * @return The recent presentations that are now stored in the document.
     * @throws ContextFileException
     */
    public static Set<RecentPresentation> purgeRecentPresentations(final InputStream input, final OutputStream output, final long numberOfRecentPresentationsToKeep) throws ContextFileException {
        Set<RecentPresentation> presentations = readRecentPresentationFromStream(input);

        if (presentations.size() > numberOfRecentPresentationsToKeep) {
            final Comparator<RecentPresentation> byDateDesc = (rp1, rp2) -> {
                int result;

                if (rp1.getOpenedDateTime() == null && rp2.getNormalizedPath() != null) {
                    result = -1;
                } else if (rp1.getOpenedDateTime() != null && rp2.getOpenedDateTime() == null) {
                    result = 1;
                } else {
                    result = -rp1.getOpenedDateTime().compareTo(rp2.getOpenedDateTime());
                }

                return result;
            };

            Set<RecentPresentation> presentationsSortedByDateDesc = new TreeSet<>(byDateDesc);
            presentationsSortedByDateDesc.addAll(presentations);

            presentationsSortedByDateDesc = presentationsSortedByDateDesc.stream()
                    .limit(numberOfRecentPresentationsToKeep)
                    .collect(Collectors.toSet());

            final Document document = createDocumentFromInput(null);
            final Node recentPresentationsNode = getRecentPresentationsNode(document);

            presentationsSortedByDateDesc.stream()
                    .map(presentation -> createNodeFromRecentPresentation(document, presentation))
                    .forEach(recentPresentationsNode::appendChild);

            writeDocument(document, output);

            presentations = new TreeSet<>(presentationsSortedByDateDesc);
        }

        return presentations;
    }

    /**
     * Get the node that contains all recent presentations, which is named {@value #RECENT_PRESENTATIONS_TAG}.
     *
     * @param document The document from which read the recent presentations.
     * @return The node containing all recent presentations or {@code null} if not found.
     */
    protected static Node getRecentPresentationsNode(final Document document) {
        Node recentPresentationsNode = null;

        final NodeList list = document.getElementsByTagName(RECENT_PRESENTATIONS_TAG);
        if (list != null && list.getLength() > 0) {
            recentPresentationsNode = list.item(0);
        }

        return recentPresentationsNode;
    }

    /**
     * Get all nodes named {@value #RECENT_PRESENTATION_TAG} within the node containing all presentations.
     *
     * @param recentPresentationsNode The node containing all recent presentations.
     * @return A never {@code null} collection of all recent presentation nodes.
     */
    protected static List<Node> getRecentPresentationNodes(final Node recentPresentationsNode) {
        final List<Node> recentPresentations = new ArrayList<>();

        final NodeList children = recentPresentationsNode.getChildNodes();
        if (children != null) {
            for (int index = 0; index < children.getLength(); index++) {
                final Node item = children.item(index);

                if (RECENT_PRESENTATION_TAG.equals(item.getNodeName())) {
                    recentPresentations.add(item);
                }
            }
        }

        return recentPresentations;
    }

    /**
     * Build a {@link RecentPresentation} object from the given {@link Node node}. In order to be created, the node
     * must contain a {@value #FILE_TAG} tag as well as a {@link #OPENED_DATE_TIME_TAG} tag. Otherwise, a {@code null}
     * object will be returned.
     *
     * @param node The node that will be used to create a {@link RecentPresentation}.
     * @return A {@link RecentPresentation} instance created from the given node.
     */
    protected static RecentPresentation buildRecentPresentationFromNode(final Node node) {
        RecentPresentation recentPresentation = null;
        LocalDateTime openedDateTime = null;
        String path = null;

        final NodeList children = node.getChildNodes();
        int index = 0;

        while (index < children.getLength() && (openedDateTime == null || path == null)) {
            final Node child = children.item(index++);

            switch (child.getNodeName()) {
                case OPENED_DATE_TIME_TAG:
                    openedDateTime = LocalDateTime.parse(child.getTextContent());
                    break;
                case FILE_TAG:
                    path = child.getTextContent();
                    break;
                default:
                    LOGGER.fine(child.getNodeName() + " not supported");
            }
        }

        if (openedDateTime != null && path != null) {
            recentPresentation = new RecentPresentation(path, openedDateTime);
        }

        return recentPresentation;
    }

    /**
     * Populate a given {@link Document} instance in order to add :<br />
     * <ul>
     * <li>the {@value #ROOT_TAG} root tag if needed;</li>
     * <li>the {@value #RECENT_PRESENTATIONS_TAG} if needed.</li>
     * </ul>
     * <p>
     * If the document already respects this structure, nothing weel be added to it.
     *
     * @param document The document to populate.
     */
    protected static void populateDocumentIfNecessary(final Document document) {
        Element root = document.getDocumentElement();
        if (root == null) {
            root = document.createElement(ROOT_TAG);
            document.appendChild(root);
        }

        try {
            final StringBuilder expression = new StringBuilder("/").append(ROOT_TAG)
                    .append("/").append(RECENT_PRESENTATIONS_TAG).append("[1]");
            Node recentPresentationsNode = (Node) XPATH.evaluate(expression.toString(), root, NODE);

            if (recentPresentationsNode == null) {
                recentPresentationsNode = document.createElement(RECENT_PRESENTATIONS_TAG);
                root.appendChild(recentPresentationsNode);
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.WARNING, "Can not parse the document properly", e);
        }
    }

    /**
     * Create a {@link Node} instance from the given {@link RecentPresentation recentPresentation}. If the presentation
     * doesn't have a path or a {@link RecentPresentation#getOpenedDateTime()}, {@code null} will be returned.
     *
     * @param document           The document that will be used to create a {@link Node node} instance.
     * @param recentPresentation The presentation to create the node for.
     * @return A well created {@link Node node} with information from the presentation.
     */
    protected static Node createNodeFromRecentPresentation(final Document document, final RecentPresentation recentPresentation) {
        if (recentPresentation == null || recentPresentation.getOpenedDateTime() == null
                || recentPresentation.getAbsolutePath() == null || recentPresentation.getAbsolutePath().trim().isEmpty()) {
            return null;
        }

        final Element recentPresentationNode = document.createElement(RECENT_PRESENTATION_TAG);
        final Element idNode = document.createElement(ID_TAG);
        final Element fileNode = document.createElement(FILE_TAG);
        final Element openedDateTimeNode = document.createElement(OPENED_DATE_TIME_TAG);

        idNode.setTextContent(recentPresentation.getId());
        fileNode.setTextContent(recentPresentation.getNormalizedPath());
        openedDateTimeNode.setTextContent(recentPresentation.getOpenedDateTime().toString());

        recentPresentationNode.appendChild(idNode);
        recentPresentationNode.appendChild(fileNode);
        recentPresentationNode.appendChild(openedDateTimeNode);

        return recentPresentationNode;
    }

    /**
     * Search a given {@link RecentPresentation recent presentation} by it's ID within the document. If the document
     * contains the presentation, it will be returned, otherwise {@code null} is returned.
     *
     * @param document           The document to search in.
     * @param recentPresentation The presentation to search by it's ID.
     * @return The node corresponding to the given presentation or {@code null} if not found.
     */
    protected static Node findRecentPresentationNodeFromID(final Document document, final RecentPresentation recentPresentation) throws ContextFileException {
        final StringBuilder expression = new StringBuilder("/").append(ROOT_TAG)
                .append("/").append(RECENT_PRESENTATIONS_TAG)
                .append("/").append(RECENT_PRESENTATION_TAG)
                .append("[").append(ID_TAG).append(" = '").append(recentPresentation.getId())
                .append("'][1]");

        try {
            return (Node) XPATH.evaluate(expression.toString(), document.getDocumentElement(), NODE);
        } catch (XPathExpressionException e) {
            throw new ContextFileException(e);
        }
    }

    /**
     * Write a given {@link Document document} to a given {@link OutputStream output}.
     *
     * @param document The document to write.
     * @param output   The output where to write the document.
     */
    protected static void writeDocument(Document document, OutputStream output) {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8.displayName());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            final DOMSource source = new DOMSource(document);
            final StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, "Can not write to the document", e);
        }
    }

    /**
     * Create an {@link InputStream} with the default body of the XML document.
     *
     * @return An {@link InputStream} with the default body of the XML document.
     */
    protected static InputStream createDefaultDocumentBodyInputStream() {
        final StringBuilder body = new StringBuilder("<").append(ROOT_TAG).append(">")
                .append("<").append(RECENT_PRESENTATIONS_TAG).append("></").append(RECENT_PRESENTATIONS_TAG).append(">")
                .append("</").append(ROOT_TAG).append(">");

        return new ByteArrayInputStream(body.toString().getBytes(getDefaultCharset()));
    }

    /**
     * Get an {@link InputStream} for a given context file. If the context file doesn't exist, then an {@link InputStream}
     * with a default XML body will be returned.
     *
     * @param contextFile The file for which create an {@link InputStream}.
     * @return An {@link InputStream} for the given context file.
     * @throws IOException If an error occurs when reading the context file.
     */
    protected static InputStream getInputStreamForFile(File contextFile) throws IOException {
        return contextFile.exists() ? new ByteArrayInputStream(Files.readAllBytes(contextFile.toPath())) : createDefaultDocumentBodyInputStream();
    }

    /**
     * Creates an instance of {@link Document} from the given {@link InputStream input}. If {@code input.available() == 0}
     * or if the given input is {@code null} then the result of the method {@link #createDefaultDocumentBodyInputStream()}
     * is used for creating the document.
     *
     * @param input The input to create the document from.
     * @return An instance of {@link Document}.
     * @throws ContextFileException
     */
    protected static Document createDocumentFromInput(InputStream input) throws ContextFileException {
        Document document;
        try {
            if (input == null || input.available() == 0) {
                document = DOCUMENT_BUILDER.parse(createDefaultDocumentBodyInputStream());
            } else {
                document = DOCUMENT_BUILDER.parse(input);
            }
        } catch (IOException | SAXException e) {
            throw new ContextFileException(e);
        }

        return document;
    }
}
