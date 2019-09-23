package eu.profinit.manta.connector.java.analysis.mybatis.handler.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.profinit.manta.connector.java.analysis.common.StrictHashMap;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader;
import eu.profinit.manta.connector.java.analysis.utils.FileContentReader.FileReadingException;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.XmlUtils;
import eu.profinit.manta.connector.java.analysis.utils.exception.XmlException;

public class MyBatisConfigurationReader {
    private static final Logger log = LoggerFactory.getLogger(MyBatisConfigurationReader.class);

    public static final String DEFAULT_ENVIRONMENT_NAME = "DefaultEnvironmentName";
    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\$\\{([a-zA-Z0-9_.-]+)}");

    private final String config;
    private final Document document;
    /**
     * Properties defined in configuration/properties/ tag of document
     */
    private final Map<String, String> properties = new HashMap<>();
    /**
     * Properties defined in configuration/environments/environment/dataSource/ tag grouped by environment name
     */
    private final Map<String, Map<String, String>> dataSourceProperties = new HashMap<>();

    /**
     * @param config Nazov konfiguracneho suboru
     * @param reader Reader pouzivany na nacitanie konfiguracneho suboru
     * @throws XmlException Ak sa subor nepodarilo naparsovat
     */
    public MyBatisConfigurationReader(@Nonnull final String config, @Nonnull final FileContentReader reader) throws XmlException {
        Validate.notNull(config);
        this.config = config;
        try {
            this.document = XmlUtils.readDocument(config, reader.readFile(config));
        } catch (FileReadingException e) {
            throw new XmlException("Failed to read file " + config, e);
        }
    }

    /**
     * @return Vrati hodnoty {@link ConnectionConfiguration} zoskupene podla nazvu environmentu
     * @throws XmlException Ak sa subor nepodarilo naparsovat
     */
    @Nonnull
    public Map<String, ConnectionConfiguration> parseConfigFile() throws XmlException {
        try {
            return parseConfigFileLocal();
        } catch (RuntimeException e) {
            throw new XmlException(String.format("Failed to parse config file %s", config), e);
        }
    }

    @Nonnull
    private Map<String, ConnectionConfiguration> parseConfigFileLocal() {
        mapProperties();
        mapDataSourceProperties();

        final List<Node> environments = XmlUtils.getNodes(document, "configuration", "environments");
        Validate.equals(environments.size(), 1, "Configuration file malformed!");

        final Map<String, ConnectionConfiguration> result = new StrictHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : dataSourceProperties.entrySet()) {
            final ConnectionConfiguration configuration = mapConfiguration(entry.getValue());
            final String environmentName = entry.getKey();

            result.put(environmentName, configuration);
        }
        final String defaultEnvironment = XmlUtils.getAttribute(environments.get(0), "default");
        result.put(DEFAULT_ENVIRONMENT_NAME, result.get(defaultEnvironment));

        log.info("Parsed configuration {}: {}", document.getDocumentURI(), result);

        return result;
    }

    private void mapProperties() {
        final List<Node> propertyNodes = XmlUtils.getNodes(document, "configuration", "properties", "property");
        for (Node propertyNode : propertyNodes) {
            properties.put(XmlUtils.getAttribute(propertyNode, "name"), XmlUtils.getAttribute(propertyNode, "value"));
        }
        log.debug("Found property mappings {}", properties);
    }

    private void mapDataSourceProperties() {
        final List<Node> propertyNodes = XmlUtils.getNodes(document, "configuration", "environments", "environment", "dataSource", "property");
        for (Node propertyNode : propertyNodes) {
            final String name = XmlUtils.getAttribute(propertyNode, "name");
            final String value = substituteVariable(XmlUtils.getAttribute(propertyNode, "value"));

            final Node environmentNode = propertyNode.getParentNode().getParentNode();
            Validate.isTrue(Objects.equals(environmentNode.getNodeName(), "environment"), "Expected environment node, found %s", environmentNode);

            final String environmentId = XmlUtils.getAttribute(environmentNode, "id");

            if (!dataSourceProperties.containsKey(environmentId)) {
                dataSourceProperties.put(environmentId, new HashMap<>());
            }
            final Map<String, String> environmentMapping = dataSourceProperties.get(environmentId);
            Validate.validState(!environmentMapping.containsKey(name), "Name %s is present in properties: %s", name, dataSourceProperties);
            environmentMapping.put(name, value);
        }
        log.debug("Found dataSource property mappings {}", dataSourceProperties);
    }

    @Nonnull
    private ConnectionConfiguration mapConfiguration(Map<String, String> environmentProperties) {
        return new ConnectionConfiguration(
                environmentProperties.get("driver"),
                environmentProperties.get("url"),
                environmentProperties.get("username"));
    }

    @Nullable
    private String substituteVariable(@Nullable final String value) {
        if (value != null) {
            // try to substitute property name inside
            final Matcher matcher = VARIABLE_REGEX.matcher(value);
            if (matcher.matches()) {
                final String variableName = matcher.group(1);
                return properties.getOrDefault(variableName, value);
            }
        }
        return value;
    }
}
