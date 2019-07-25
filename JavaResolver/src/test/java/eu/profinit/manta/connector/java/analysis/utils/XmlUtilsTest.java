package eu.profinit.manta.connector.java.analysis.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import eu.profinit.manta.connector.java.analysis.AbstractTest;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class XmlUtilsTest extends AbstractTest {
    @Test(dataProvider = "testGetNodesDataProvider")
    public void testGetNodes(String documentContent, List<String> path, List<NodeNameWithAttributes> expectedNodeNamesForPath) throws XmlException {
        final Document document = XmlUtils.readDocument("Id", documentContent);
        final List<Node> nodes = XmlUtils.getNodes(document, path.toArray(new String[0]));
        final List<NodeNameWithAttributes> actualValues = nodes.stream()
                .map(node -> new NodeNameWithAttributes(node.getNodeName(), getAttributes(node.getAttributes())))
                .collect(Collectors.toList());

        assertReflectionEquals(expectedNodeNamesForPath, actualValues);
    }

    @Nonnull
    private Attribute[] getAttributes(NamedNodeMap attributes) {
        return IntStream.range(0, attributes.getLength())
                .mapToObj(attributes::item)
                .map(attribute -> new Attribute(attribute.getNodeName(), attribute.getNodeValue()))
                .toArray(Attribute[]::new);
    }

    @DataProvider
    private Object[][] testGetNodesDataProvider() {
        // @formatter:off
        final String content = ""
                          + "<a x='1'>"
                            + "<b y='2'>"
                                + "<c z='3'></c>"
                                + "<c z='3.1'/>"
                            + "</b>"
                            + "<b y='2.1'>"
                                + "<c z='3.2'></c>"
                                + "<c z='3.3'/>"
                            + "</b>"
                            + "<b>"
                                + "<c z='3.4'></c>"
                                + "<c z='3.5'/>"
                            + "</b>"
                            + "<b/>"
                          + "</a>";
        // @formatter:on
        return new Object[][] {
                {
                        content,
                        Lists.newArrayList(),
                        Lists.newArrayList(new NodeNameWithAttributes("a", new Attribute("x", "1")))
                },
                {
                        content,
                        Lists.newArrayList("a"),
                        Lists.newArrayList(new NodeNameWithAttributes("a", new Attribute("x", "1")))
                },
                {
                        content,
                        Lists.newArrayList("a", "b"),
                        Lists.newArrayList(
                                new NodeNameWithAttributes("b", new Attribute("y", "2")),
                                new NodeNameWithAttributes("b", new Attribute("y", "2.1")),
                                new NodeNameWithAttributes("b"),
                                new NodeNameWithAttributes("b")
                        )
                },
                {
                        content,
                        Lists.newArrayList("a", "b", "c"),
                        Lists.newArrayList(
                                new NodeNameWithAttributes("c", new Attribute("z", "3")),
                                new NodeNameWithAttributes("c", new Attribute("z", "3.1")),
                                new NodeNameWithAttributes("c", new Attribute("z", "3.2")),
                                new NodeNameWithAttributes("c", new Attribute("z", "3.3")),
                                new NodeNameWithAttributes("c", new Attribute("z", "3.4")),
                                new NodeNameWithAttributes("c", new Attribute("z", "3.5"))
                        )
                },
                {
                        content,
                        Lists.newArrayList("not-exist-key"),
                        Lists.newArrayList()
                }
        };
    }

    private static class Attribute {
        private final String name;
        private final String value;

        private Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("name", name)
                    .add("value", value)
                    .toString();
        }
    }

    private static class NodeNameWithAttributes {
        private final String name;
        private final List<Attribute> attributes;

        private NodeNameWithAttributes(String name, Attribute... attributes) {
            this.name = name;
            this.attributes = Arrays.asList(attributes);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("name", name)
                    .add("attributes", attributes)
                    .toString();
        }
    }
}