package eu.profinit.manta.connector.java.analysis.mybatis.handler.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.common.base.Objects;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.XmlUtils;

public class XmlTagHandlers {
    private static final Logger log = LoggerFactory.getLogger(XmlTagHandlers.class);

    public static final TagByNameXmlHandler IF_HANDLER;
    public static final TagByNameXmlHandler FOREACH_HANDLER;
    public static final TagByNameXmlHandler CHOOSE_HANDLER;
    public static final TagByNameXmlHandler TRIM_HANDLER;
    public static final TagByNameXmlHandler WHERE_HANDLER;
    public static final TagByNameXmlHandler SET_HANDLER;
    public static final XmlTagHandler RAW_TEXT_HANDLER;
    public static final XmlTagHandler COMMENT_HANDLER;

    private final List<XmlTagHandler> handlers;

    static {
        // initialize variables
        IF_HANDLER = new TagByNameXmlHandler("if");
        FOREACH_HANDLER = new TagByNameXmlHandler("foreach") {
            @Override
            public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
                final String openValue = XmlUtils.getAttribute(node, "open", "");
                final String closeValue = XmlUtils.getAttribute(node, "close", "");

                return Stream.concat(Stream.concat(
                        Stream.of(openValue),
                        handlers.handle(node.getChildNodes())),
                        Stream.of(closeValue));
            }
        };
        CHOOSE_HANDLER = new TagByNameXmlHandler("choose") {
            @Override
            public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
                // take first when/otherwise element as if condition was met
                final Node childNode = Stream.concat(
                        XmlUtils.getNodes(node, "when"),
                        XmlUtils.getNodes(node, "otherwise"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("<choose> element should contain when/otherwise tags!"));

                return handlers.handle(childNode.getChildNodes());
            }
        };
        TRIM_HANDLER = new AbstractTrimXmlTagHandler("trim") {
            @Override
            protected String getCommandName(@Nonnull final Node node) {
                return XmlUtils.getAttribute(node, "prefix");
            }

            @Override
            protected String getSuffixesToOverride(@Nonnull final Node node) {
                return XmlUtils.getAttribute(node, "suffixOverrides");
            }

            @Override
            protected String getPrefixesToOverride(@Nonnull final Node node) {
                return XmlUtils.getAttribute(node, "prefixOverrides");
            }
        };
        WHERE_HANDLER = new AbstractTrimXmlTagHandler("where") {
            @Override
            protected String getCommandName(@Nonnull final Node node) {
                return "WHERE";
            }

            @Override
            protected String getSuffixesToOverride(@Nonnull final Node node) {
                return null;
            }

            @Override
            protected String getPrefixesToOverride(@Nonnull final Node node) {
                return "AND |OR ";
            }
        };
        SET_HANDLER = new AbstractTrimXmlTagHandler("set") {
            @Override
            protected String getCommandName(@Nonnull final Node node) {
                return "SET";
            }

            @Override
            protected String getSuffixesToOverride(@Nonnull final Node node) {
                return ",";
            }

            @Override
            protected String getPrefixesToOverride(@Nonnull final Node node) {
                return null;
            }
        };
        RAW_TEXT_HANDLER = new XmlTagHandler() {
            @Override
            public boolean canHandle(@Nonnull final Node node) {
                return node instanceof Text;
            }

            @Override
            public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
                return Stream.of(((Text) node).getWholeText());
            }

            @Override
            public String toString() {
                return "RawTextXmlTagHandler";
            }
        };
        COMMENT_HANDLER = new XmlTagHandler() {
            @Override
            public boolean canHandle(@Nonnull Node node) {
                return node instanceof Comment;
            }

            @Override
            public Stream<String> handle(@Nonnull Node node, @Nonnull XmlTagHandlers handlers) {
                // ignore
                return Stream.empty();
            }

            @Override
            public String toString() {
                return "CommentXmlTagHandler";
            }
        };
    }

    public XmlTagHandlers(@Nonnull final XmlTagHandler... handlers) {
        this.handlers = Arrays.asList(handlers);
    }

    public Stream<String> handle(@Nonnull final NodeList nodes) {
        return XmlUtils.toList(nodes).stream()
                .flatMap(this::handle);
    }

    public Stream<String> handle(@Nonnull final Node node) {
        final List<XmlTagHandler> supportedHandlers = handlers.stream()
                .filter(handler -> handler.canHandle(node))
                .collect(Collectors.toList());
        log.trace("Found handlers {} for <{}> tag in MyBatis", supportedHandlers, node.getNodeName());

        if (supportedHandlers.isEmpty()) {
            log.warn("Found no handler for <{}> tag. Supported are {}", node.getNodeName(), handlers);
        } else if (supportedHandlers.size() > 1) {
            log.warn("Found more handlers for <{}> tag. Supported are {}", node.getNodeName(), handlers);
        }
        return supportedHandlers.stream()
                .flatMap(handler -> handler.handle(node, this));
    }

    public interface XmlTagHandler {
        boolean canHandle(@Nonnull final Node node);

        Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers);
    }

    public static class TagByNameXmlHandler implements XmlTagHandler {
        private final String tagName;

        public TagByNameXmlHandler(@Nonnull final String tagName) {
            Validate.notNull(tagName);
            this.tagName = tagName;
        }

        @Override
        public boolean canHandle(@Nonnull final Node node) {
            return Objects.equal(node.getNodeName(), tagName);
        }

        @Override
        public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
            return handlers.handle(node.getChildNodes());
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("tagName", tagName)
                    .toString();
        }
    }

    public static class IncludeXmlTagHandler extends TagByNameXmlHandler {
        private final Document document;

        public IncludeXmlTagHandler(@Nonnull final Document document) {
            super("include");
            Validate.notNull(document);
            this.document = document;
        }

        @Override
        public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
            final String sqlId = XmlUtils.getAttribute(node, "refid");
            final Node sqlNode = XmlUtils.getNodes(document, "mapper", "sql").stream()
                    .filter(childNode -> Objects.equal(XmlUtils.getAttribute(childNode, "id"), sqlId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Not known sql reference to " + sqlId));

            return handlers.handle(sqlNode.getChildNodes());
        }
    }

    protected static abstract class AbstractTrimXmlTagHandler extends TagByNameXmlHandler {
        private static final String DELIMITER = "\\|";

        public AbstractTrimXmlTagHandler(@Nonnull final String tagName) {
            super(tagName);
        }

        protected abstract String getCommandName(@Nonnull final Node node);

        protected abstract String getSuffixesToOverride(@Nonnull final Node node);

        protected abstract String getPrefixesToOverride(@Nonnull final Node node);

        @Override
        public Stream<String> handle(@Nonnull final Node node, @Nonnull final XmlTagHandlers handlers) {
            final String command = getCommandName(node);
            final String suffixOverrides = getSuffixesToOverride(node);
            final String prefixOverrides = getPrefixesToOverride(node);

            Validate.notNull(command);

            String sqlCommand = handlers.handle(node.getChildNodes()).collect(Collectors.joining("")).replaceAll("\\s+", " ");
            if (StringUtils.isNotEmpty(prefixOverrides)) {
                for (String prefix : prefixOverrides.split(DELIMITER)) {
                    sqlCommand = sqlCommand.replaceAll(String.format("^\\s*%s", prefix), "");
                }
            }
            if (StringUtils.isNotEmpty(suffixOverrides)) {
                for (String suffix : suffixOverrides.split(DELIMITER)) {
                    sqlCommand = sqlCommand.replaceAll(String.format("%s\\s*$", suffix), "");
                }
            }
            return Stream.of(command, " ", sqlCommand);
        }
    }
}
