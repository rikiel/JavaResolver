package eu.profinit.manta.connector.java.analysis.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;

public final class XmlUtils {
    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = createBuilderFactory();

    private XmlUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param documentId      Document name
     * @param documentContent Document content
     * @return Returns XML {@link Document} with the content as in {@code documentContent}
     * @throws XmlException When content fails to be parsed
     */
    @Nonnull
    public static Document readDocument(@Nonnull final String documentId, @Nonnull final String documentContent) throws XmlException {
        Validate.notNullAll(documentId, documentContent);
        try {
            log.trace("Parsing xml document {}:\n{}", documentId, documentContent);

            final DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final Document document = documentBuilder.parse(new ReaderInputStream(new StringReader(documentContent)));
            document.normalizeDocument();
            document.setDocumentURI(documentId);

            return document;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new XmlException("Could not parse document " + documentId, e);
        }
    }

    /**
     * @param nodeList XML nodes to transform
     * @return Returns list representation of nodes
     */
    @Nonnull
    public static List<Node> toList(@Nonnull final NodeList nodeList) {
        Validate.notNull(nodeList);
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .collect(Collectors.toList());
    }

    /**
     * @param document Document
     * @param path     XML tags
     * @return List of nodes in XML document with tags as in {@code path}
     */
    @Nonnull
    public static List<Node> getNodes(@Nonnull final Document document, @Nonnull final String... path) {
        Validate.notNullAll(document, path);

        if (path.length == 0) {
            return toList(document.getChildNodes());
        }

        final List<String> subpath = Lists.newArrayList(path);
        final String nodeName = subpath.remove(0);

        return toList(document.getChildNodes()).stream()
                .filter(node -> Objects.equals(node.getNodeName(), nodeName))
                .flatMap(node -> getNodes(node, subpath))
                .collect(Collectors.toList());
    }

    /**
     * @param rootNode Node that we want to search in
     * @param path     XML tags
     * @return List of nodes in subtree of {@code rootNode} with XML tags as in {@code path}
     */
    @Nonnull
    public static Stream<Node> getNodes(@Nonnull final Node rootNode, @Nonnull final String... path) {
        return getNodes(rootNode, Arrays.asList(path));
    }

    /**
     * @param node          Node
     * @param attributeName Attribute name
     * @return Value of attribute
     */
    @Nullable
    public static String getAttribute(@Nonnull final Node node, @Nonnull final String attributeName) {
        return getAttribute(node, attributeName, null);
    }

    /**
     * @param node           Node
     * @param attributeName  Attribute name
     * @param defaultForNull Value to return if attribute is not present in node
     * @return Value of attribute, or {@code defaultForNull} if attribute is not present
     */
    @Nullable
    public static String getAttribute(@Nonnull final Node node, @Nonnull final String attributeName, @Nullable final String defaultForNull) {
        final Node attributeNode = node.getAttributes().getNamedItem(attributeName);
        return attributeNode == null
                ? defaultForNull
                : attributeNode.getNodeValue();
    }

    @Nonnull
    private static Stream<Node> getNodes(@Nonnull final Node rootNode, @Nonnull final List<String> path) {
        if (path.isEmpty()) {
            return Stream.of(rootNode);
        }
        final List<String> subpath = Lists.newArrayList(path);
        final String nodeName = subpath.remove(0);

        return toList(rootNode.getChildNodes()).stream()
                .filter(child -> Objects.equals(nodeName, child.getNodeName()))
                .flatMap(child -> getNodes(child, subpath));
    }

    @Nonnull
    private static DocumentBuilderFactory createBuilderFactory() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return factory;
        } catch (ParserConfigurationException e) {
            return Validate.fail("Failed to create DocumentBuilderFactory");
        }
    }
}
