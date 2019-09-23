package eu.profinit.manta.connector.java.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.profinit.manta.connector.java.model.IEdge;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.model.INode;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

public class TestUtil {
    private static boolean nodesShareSameAttributeValue(INode n1, INode n2, String attrName) {
        Set<Object> n1AttrVals = n1.getAttributeValues(attrName);
        Set<Object> n2AttrVals = n2.getAttributeValues(attrName);
        for (Object n1av : n1AttrVals) {
            for (Object n2av : n2AttrVals) {
                if (n1av.equals(n2av)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void assertNodeExistsInGraph(IGraph iGraph, NodeSpecification... nodes) {
        final List<NodeSpecification> notExistedNodes = Arrays.stream(nodes)
                .filter(node -> node.getMatchingNodes(iGraph).isEmpty())
                .collect(Collectors.toList());

        Assert.assertTrue("Nodes DOES NOT exist in graph, but should: " + notExistedNodes,
                notExistedNodes.isEmpty());
    }

    public static void assertNodeNotExistsInGraph(IGraph iGraph, NodeSpecification... nodes) {
        final List<NodeSpecification> existedNodes = Arrays.stream(nodes)
                .filter(node -> !node.getMatchingNodes(iGraph).isEmpty())
                .collect(Collectors.toList());

        Assert.assertTrue("Nodes DOES exist in graph, but should not: " + existedNodes, existedNodes.isEmpty());
    }

    public static void assertFlowExistsInGraph(IGraph iGraph, NodeSpecification sourceNode,
                                               NodeSpecification targetNode) {
        assertFlowExistsInGraph(iGraph, Lists.newArrayList(sourceNode), Lists.newArrayList(targetNode));
    }

    public static void assertFlowExistsInGraph(IGraph iGraph, NodeSpecification sourceNode,
                                               List<NodeSpecification> targetNodes) {
        assertFlowExistsInGraph(iGraph, Lists.newArrayList(sourceNode), targetNodes);
    }

    public static void assertFlowExistsInGraph(IGraph iGraph, List<NodeSpecification> sourceNodes,
                                               NodeSpecification targetNode) {
        assertFlowExistsInGraph(iGraph, sourceNodes, Lists.newArrayList(targetNode));
    }

    public static void assertFlowExistsInGraph(IGraph iGraph, List<NodeSpecification> sourceNodes,
                                               List<NodeSpecification> targetNodes) {
        final List<Pair<NodeSpecification, NodeSpecification>> notExistedPath = new LinkedList<>();
        for (NodeSpecification source : sourceNodes) {
            for (NodeSpecification target : targetNodes) {
                if (!flowExistsInGraph(iGraph, source, target)) {
                    notExistedPath.add(Pair.of(source, target));
                }
            }
        }
        Assert.assertTrue("Some paths do not exist in graph: " + notExistedPath, notExistedPath.isEmpty());
    }

    public static void assertFlowNotExistsInGraph(IGraph iGraph, NodeSpecification sourceNode,
                                                  NodeSpecification targetNode) {
        assertFlowNotExistsInGraph(iGraph, Lists.newArrayList(sourceNode), Lists.newArrayList(targetNode));
    }

    public static void assertFlowNotExistsInGraph(IGraph iGraph, NodeSpecification sourceNode,
                                                  List<NodeSpecification> targetNodes) {
        assertFlowNotExistsInGraph(iGraph, Lists.newArrayList(sourceNode), targetNodes);
    }

    public static void assertFlowNotExistsInGraph(IGraph iGraph, List<NodeSpecification> sourceNodes,
                                                  NodeSpecification targetNode) {
        assertFlowNotExistsInGraph(iGraph, sourceNodes, Lists.newArrayList(targetNode));
    }

    public static void assertFlowNotExistsInGraph(IGraph iGraph, List<NodeSpecification> sourceNodes,
                                                  List<NodeSpecification> targetNodes) {
        final List<Pair<NodeSpecification, NodeSpecification>> existedPath = new LinkedList<>();
        for (NodeSpecification source : sourceNodes) {
            for (NodeSpecification target : targetNodes) {
                if (flowExistsInGraph(iGraph, source, target)) {
                    existedPath.add(Pair.of(source, target));
                }
            }
        }
        Assert.assertTrue("Some paths exist in graph: " + existedPath, existedPath.isEmpty());
    }

    /**
     * @return Vrati, ci existuje cesta v grafe z nejakeho vrcholu identifikovaneho
     *         {@code sourceSpecification} do nejakeho vrcholu identifikovaneho
     *         {@code targetSpecification}. V oboch pripadoch moze existovat viac
     *         zdrojovych/cielovych vrcholov, ale hladame aspon jednu cestu
     */
    private static boolean flowExistsInGraph(IGraph iGraph, NodeSpecification sourceSpecification, NodeSpecification targetSpecification) {
        Validate.notNull(iGraph);
        Validate.notNull(sourceSpecification);
        Validate.notNull(targetSpecification);

        final Set<INode> sourceNodes = sourceSpecification.getMatchingNodes(iGraph);
        final Set<INode> targetNodes = targetSpecification.getMatchingNodes(iGraph);

        for (INode sourceNode : sourceNodes) {
            for (INode targetNode : targetNodes) {
                final Queue<INode> nodesToSearch = Lists.newLinkedList(Collections.singletonList(sourceNode));
                final Set<INode> alreadySearchedNodes = Sets.newHashSet(sourceNode);

                while (!nodesToSearch.isEmpty()) {
                    final INode node = nodesToSearch.poll();
                    if (Objects.equals(node, targetNode)) {
                        return true;
                    }
                    final List<INode> edgeNodes = iGraph.getEdgesFrom(node).stream().map(IEdge::getTarget)
                            .filter(alreadySearchedNodes::add).collect(Collectors.toList());
                    nodesToSearch.addAll(edgeNodes);
                }
            }
        }

        return false;
    }

    public interface NodeSpecification {
        boolean matches(INode iNode);

        default Set<INode> getMatchingNodes(final IGraph iGraph) {
            return iGraph.getNodes().stream().filter(this::matches).collect(Collectors.toSet());
        }
    }

    /**
     * Specification of node's name.
     */
    public static class NodeNameSpecification implements NodeSpecification {
        private final Set<String> requiredNameParts;

        public NodeNameSpecification(final String... requiredNameParts) {
            this.requiredNameParts = Sets.newHashSet(requiredNameParts);
        }

        @Override
        public boolean matches(final INode iNode) {
            final String nodeName = iNode.getName();
            for (String requiredNamePart : requiredNameParts) {
                if (!(nodeName.contains(requiredNamePart))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("requiredNameParts", requiredNameParts)
                    .build();
        }
    }

    /**
     * Specification of node's attributes (attribute name and attribute value).
     */
    public static class NodeAttributeSpecification implements NodeSpecification {
        public final Map<String, Set<String>> requiredAttributeValues;
        public final Set<String> forbiddenAttributes;

        public NodeAttributeSpecification(Map<String, Set<String>> requiredAttributeValues,
                Set<String> forbiddenAttributes) {
            this.requiredAttributeValues = requiredAttributeValues;
            this.forbiddenAttributes = forbiddenAttributes;
        }

        public NodeAttributeSpecification(String attributeName, String attributeValue) {
            this(Collections.singletonMap(attributeName, Collections.singleton(attributeValue)),
                    Collections.emptySet());
        }

        public NodeAttributeSpecification(IAttributeName attributeName, String... attributeValues) {
            this(Collections.singletonMap(attributeName.getAttributeName(), ImmutableSet.copyOf(attributeValues)),
                    ImmutableSet.of());
        }

        @Override
        public boolean matches(final INode iNode) {
            for (String forbiddenAttribute : forbiddenAttributes) {
                if (iNode.hasAttribute(forbiddenAttribute)) {
                    return false;
                }
            }
            for (Map.Entry<String, Set<String>> attributeEntry : requiredAttributeValues.entrySet()) {
                final boolean hasAttribute = iNode.getAttributeValues(attributeEntry.getKey()).stream()
                        .map(Object::toString)
                        .anyMatch(attribute -> {
                            for (String expectedValuePart : attributeEntry.getValue()) {
                                if (!attribute.contains(expectedValuePart)) {
                                    return false;
                                }
                            }
                            return true;
                        });
                if (!hasAttribute) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("requiredAttributeValues", requiredAttributeValues)
                    .append("forbiddenAttributes", forbiddenAttributes)
                    .build();
        }
    }

    /**
     * Specification for composing more types of {@link NodeSpecification}
     */
    public static class NodeComposedSpecification implements NodeSpecification {
        private final List<NodeSpecification> specificationList;

        public NodeComposedSpecification(@Nonnull final NodeSpecification... nodeSpecifications) {
            Validate.notNull(nodeSpecifications);
            Validate.isTrue(nodeSpecifications.length > 1, "Expected more than 1 composing specification");
            this.specificationList = Arrays.asList(nodeSpecifications);
        }

        @Override
        public boolean matches(INode iNode) {
            return specificationList.stream()
                    .allMatch(node -> node.matches(iNode));
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("specificationList", specificationList)
                    .toString();
        }
    }
}
