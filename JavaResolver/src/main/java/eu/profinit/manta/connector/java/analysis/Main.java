package eu.profinit.manta.connector.java.analysis;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.profinit.manta.connector.java.analysis.datasource.DataSourceAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.jdbctemplate.JdbcTemplateAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.kafka.KafkaAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.mybatis.MyBatisAnalysisPlugin;
import eu.profinit.manta.connector.java.analysis.utils.CollectionToString;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.resolver.TaskExecutor;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            TaskExecutor.run("Data Lineage computation", () -> {
                parseArguments(args)
                        .addStdlib()
                        .addExclusions()

                        .addPlugin(new DataSourceAnalysisPlugin())
                        .addPlugin(new JdbcTemplateAnalysisPlugin())
                        .addPlugin(new KafkaAnalysisPlugin())
                        .addPlugin(new MyBatisAnalysisPlugin())

                        .run()
                        .generateVisualization();
            });
        } catch (Exception e) {
            System.exit(1);
        }
    }

    @Nonnull
    private static ApplicationConfiguration parseArguments(@Nonnull final String[] args) {
        log.trace("Using arguments {}", new CollectionToString<>(args));

        final ApplicationConfiguration configuration = new ApplicationConfiguration();

        final Set<Option> wasSet = Sets.newHashSet();
        for (int i = 0; i < args.length; ++i) {
            final Option option = Option.forName(args[i]);
            wasSet.add(option);
            i = option.handle(args, i, configuration);
        }

        final Set<Option> notSetOptions = Sets.difference(
                Option.getRequiredOptions(),
                wasSet);
        Validate.isTrue(notSetOptions.isEmpty(), "Required options were not set: %s", notSetOptions);

        return configuration;
    }

    private enum Option {
        ENTRY("--entry", true) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                validateArgumentsCount(args, i + 2);
                final String methodName;
                if (Objects.equals("main", args[i + 2])) {
                    methodName = "main([Ljava/lang/String;)V";
                } else if (args[i + 2].contains("(")) {
                    methodName = args[i + 2];
                } else {
                    Validate.isFalse(args[i + 2].contains("("), "Plain name of method, not full signature, should be used. Found '%s'", args[i + 2]);
                    methodName = args[i + 2] + "()V";
                }
                configuration.addTargetEntryMethodSignature(args[i + 1], methodName);
                return i + 2;
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--entry <className> <methodName>";
            }
        },
        APPLICATION_JAR_FILE("--application-jar", true) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                validateArgumentsCount(args, i + 1);
                configuration.addJarFile(args[i + 1]);
                return i + 1;
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--application-jar <fileName>";
            }
        },
        LIBRARY_JAR_FILE("--library-jar", false) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                validateArgumentsCount(args, i + 1);
                configuration.addJarFile(args[i + 1]);
                return i + 1;
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--library-jar <fileName>";
            }
        },
        APPLICATION_PACKAGE("--application-package", true) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                validateArgumentsCount(args, i + 1);
                configuration.addApplicationPackagePrefix(args[i + 1]);
                return i + 1;
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--application-package <package>";
            }
        },
        OUTPUT_DIRECTORY("--output-directory", true) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                validateArgumentsCount(args, i + 1);
                configuration.addGraphOutputDir(args[i + 1]);
                return i + 1;
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--output-directory <directoryName>";
            }
        },
        HELP("--help", false) {
            @Override
            public int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration) {
                usage();
                System.exit(0);
                return Validate.fail("cannot happen");
            }

            @Nonnull
            @Override
            public String usageMessage() {
                return "--help";
            }
        };

        private final String optionName;
        private final boolean required;

        Option(String optionName, boolean required) {
            this.optionName = optionName;
            this.required = required;
        }

        public abstract int handle(@Nonnull final String[] args, final int i, @Nonnull final ApplicationConfiguration configuration);

        @Nonnull
        public abstract String usageMessage();

        void validateArgumentsCount(final String[] args, final int atLeast) {
            if (args.length <= atLeast) {
                usage();
                Validate.fail("Not enough arguments were provided for option %s", optionName);
            }
        }

        @Nonnull
        private static Option forName(@Nonnull final String optionName) {
            for (Option value : values()) {
                if (Objects.equals(optionName, value.optionName)) {
                    return value;
                }
            }
            return Validate.fail("Failed to parse option %s", optionName);
        }

        private static void usage() {
            StringBuilder builder = new StringBuilder("USAGE:");
            for (Option value : values()) {
                builder.append("\n\t")
                        .append(value.usageMessage());
            }
            log.info("{}", builder);
        }

        @Nonnull
        private static Set<Option> getRequiredOptions() {
            return Arrays.stream(values())
                    .filter(option -> option.required)
                    .collect(Collectors.toSet());
        }

        @Override
        public String toString() {
            return optionName;
        }
    }
}
