package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.MyBatisUtils.MyBatisSqlVariable.Mode;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.IncludeXmlTagHandler;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlCommand.CommandType;
import eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.model.SqlMapping;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader.FileReadingException;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.XmlUtils;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;
import eu.profinit.manta.connector.java.model.flowgraph.AttributeValueConstants;

import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.CHOOSE_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.COMMENT_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.FOREACH_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.IF_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.RAW_TEXT_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.SET_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.TRIM_HANDLER;
import static eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper.XmlTagHandlers.WHERE_HANDLER;

class MyBatisXmlMapperSqlReader extends MyBatisMapperSqlReader {
    private static final Logger log = LoggerFactory.getLogger(MyBatisXmlMapperSqlReader.class);

    private final Document document;
    private final Node actionNode;
    private final XmlTagHandlers xmlTagHandlers;

    MyBatisXmlMapperSqlReader(@Nonnull final IMethod iMethod,
                              @Nonnull final FileContentReader reader) {
        super(iMethod);

        Document document;
        Node actionNode;
        XmlTagHandlers xmlTagHandlers;
        try {
            final String fileName = iMethod.getDeclaringClass().getName().toString().substring(1) + ".xml";
            final String mapperFileContent = reader.readFile(fileName);

            document = XmlUtils.readDocument(fileName, mapperFileContent);
            final List<Node> commandNodes = Stream.of(
                    XmlUtils.getNodes(document, "mapper", "insert"),
                    XmlUtils.getNodes(document, "mapper", "update"),
                    XmlUtils.getNodes(document, "mapper", "delete"),
                    XmlUtils.getNodes(document, "mapper", "select"))
                    .flatMap(Collection::stream)
                    .filter(node -> Objects.equals(XmlUtils.getAttribute(node, "id"), iMethod.getName().toString()))
                    .collect(Collectors.toList());

            if (commandNodes.size() == 1) {
                actionNode = commandNodes.get(0);
                xmlTagHandlers = new XmlTagHandlers(
                        IF_HANDLER,
                        FOREACH_HANDLER,
                        CHOOSE_HANDLER,
                        TRIM_HANDLER,
                        WHERE_HANDLER,
                        SET_HANDLER,
                        RAW_TEXT_HANDLER,
                        COMMENT_HANDLER,
                        new IncludeXmlTagHandler(document));
            } else {
                if (commandNodes.isEmpty()) {
                    log.trace("Could not find action insert/update/delete/select action for method {}", iMethod.getSignature());
                } else {
                    log.error("Cannot handle more commands. Found {}", commandNodes);
                }
                actionNode = null;
                xmlTagHandlers = null;
            }
        } catch (XmlException | FileReadingException e) {
            document = null;
            actionNode = null;
            xmlTagHandlers = null;
        }
        this.document = document;
        this.actionNode = actionNode;
        this.xmlTagHandlers = xmlTagHandlers;
    }

    boolean accepts() {
        return actionNode != null;
    }

    @Nonnull
    @Override
    protected String getSql() {
        // actionNode = select/update/delete/insert tag node
        return xmlTagHandlers.handle(actionNode.getChildNodes()).collect(Collectors.joining(""));
    }

    @Nonnull
    @Override
    protected CommandType getCommandType() {
        switch (actionNode.getNodeName()) {
            case "select":
                return CommandType.SELECT;
            case "insert":
                return CommandType.INSERT;
            case "delete":
                return CommandType.DELETE;
            case "update":
                return CommandType.UPDATE;
            default:
                return Validate.fail("Not known node %s", actionNode);
        }
    }

    @Nonnull
    @Override
    protected SqlMapping getResultMapping() {
        // result map from actual query statement tag
        final SqlMapping resultMapping = new SqlMapping();
        final String resultMapId = XmlUtils.getAttribute(actionNode, "resultMap");
        if (resultMapId != null) {
            addMapping(resultMapping, resultMapId);
        }
        return resultMapping;
    }

    @Nonnull
    @Override
    protected List<SqlMapping> getArgumentsMapping() {
        return MyBatisUtils.getArgumentsFromSql(getSql()).stream()
                .filter(argument -> !Mode.IN.equals(argument.getMode()))
                .filter(argument -> {
                    if (argument.getResultMapName() == null) {
                        log.warn("Ignoring variable in !=IN mode without name for result map: {}", argument);
                        return false;
                    }
                    return true;
                })
                .map(argument -> addMapping(new SqlMapping(argument.getVariableName()), argument.getResultMapName()))
                .collect(Collectors.toList());
    }

    @Nonnull
    private SqlMapping addMapping(@Nonnull final SqlMapping mapping, @Nonnull final String resultMapId) {
        final Node resultMap = XmlUtils.getNodes(document, "mapper", "resultMap").stream()
                .filter(node -> Objects.equals(XmlUtils.getAttribute(node, "id"), resultMapId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find resultMap " + resultMapId));

        // add setters mapping
        XmlUtils.getNodes(resultMap, "result")
                .forEach(node -> mapping.addPropertyToColumn(
                        Validate.notNull(XmlUtils.getAttribute(node, "property")),
                        Validate.notNull(XmlUtils.getAttribute(node, "column"))));

        // add constructor mapping, but without property name, as we do not know it
        XmlUtils.getNodes(resultMap, "constructor", "idArg")
                .forEach(node -> mapping.addPropertyToColumn(
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT,
                        Validate.notNull(XmlUtils.getAttribute(node, "column"))
                ));
        XmlUtils.getNodes(resultMap, "constructor", "arg")
                .forEach(node -> mapping.addPropertyToColumn(
                        AttributeValueConstants.UNDEFINED_VALUE_PART_CONSTANT,
                        Validate.notNull(XmlUtils.getAttribute(node, "column"))
                ));

        return mapping;
    }
}
