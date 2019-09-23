package eu.profinit.manta.connector.java.analysis;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import cz.cuni.mff.d3s.manta.Configuration;
import eu.profinit.manta.connector.java.analysis.utils.CollectionToString;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.IGraph;
import eu.profinit.manta.connector.java.resolver.ConfigurationBuilder;
import eu.profinit.manta.connector.java.resolver.FrameworkAnalysisPlugin;
import eu.profinit.manta.connector.java.resolver.visualizer.AnalysisResultsVisualizer;

/**
 * Wrapper for {@link ConfigurationBuilder} to simplify usage in Java Resolver tool
 */
@SuppressWarnings("UnusedReturnValue")
public class ApplicationConfiguration extends ConfigurationBuilder {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private final List<String> walaScopeFileContent = Lists.newArrayList();
    private String outputDir;
    private Configuration configuration;
    private IGraph iGraph;

    @Override
    public ApplicationConfiguration setExternalLibFlowSpecification(String filePath) {
        super.setExternalLibFlowSpecification(filePath);
        return this;
    }

    @Override
    public ApplicationConfiguration addTargetEntryMethodSignature(String className, String methodName) {
        super.addTargetEntryMethodSignature(className, methodName);
        return this;
    }

    @Override
    public ApplicationConfiguration addTargetEntryMethodSignature(@Nonnull final Class<?> clazz, @Nonnull final String methodName) {
        super.addTargetEntryMethodSignature(clazz, methodName);
        return this;
    }

    @Override
    public ApplicationConfiguration addWalaScopeFilePath(String path) {
        super.addWalaScopeFilePath(path);
        return this;
    }

    @Override
    public ApplicationConfiguration addWalaExclusionFilePath(String path) {
        super.addWalaExclusionFilePath(path);
        return this;
    }

    @Override
    public ApplicationConfiguration addIgnoreLibraryPackagePrefix(String prefix) {
        super.addIgnoreLibraryPackagePrefix(prefix);
        return this;
    }

    @Override
    public ApplicationConfiguration addApplicationPackagePrefix(String prefix) {
        super.addApplicationPackagePrefix(prefix);
        return this;
    }

    @Override
    public ApplicationConfiguration addApplicationPackagePrefix(Class<?> clazz) {
        super.addApplicationPackagePrefix(clazz);
        return this;
    }

    @Override
    public ApplicationConfiguration addJrePath(String jrePath) {
        super.addJrePath(jrePath);
        return this;
    }

    @Override
    public ApplicationConfiguration addPlugin(FrameworkAnalysisPlugin plugin) {
        super.addPlugin(plugin);
        return this;
    }

    public ApplicationConfiguration run() {
        addWalaScopeFilePath(createTempFile("WalaScope", walaScopeFileContent));
        this.configuration = build();
        this.iGraph = eu.profinit.manta.connector.java.resolver.Main.runDataFlowAnalysis(configuration).flowGraph;
        return this;
    }

    public ApplicationConfiguration generateVisualization() {
        AnalysisResultsVisualizer.visualize(
                String.format("%s/%s-%s.dot",
                        outputDir,
                        configuration.targetMainClassName.replaceAll(".*\\.", ""),
                        configuration.targetEntryMethodNameDesc.replaceAll("\\(.*", "")),
                iGraph);
        return this;
    }

    public ApplicationConfiguration addStdlib() {
        walaScopeFileContent.add("Primordial,Java,stdlib,none");
        return this;
    }

    public ApplicationConfiguration addExclusions() {
        addWalaExclusionFilePath(createTempFile("WalaExclusion", ImmutableList.of(
                "sun\\/.*",
                "com\\/sun\\/.*",
                "java\\/awt\\/.*",
                "javax\\/swing\\/.*",
                "sun\\/awt\\/.*",
                "sun\\/swing\\/.*")));
        addIgnoreLibraryPackagePrefix("org.springframework.jdbc.datasource.embedded.DerbyEmbeddedDatabaseConfigurer");
        return this;
    }

    public IGraph getGraph() {
        return iGraph;
    }

    public ApplicationConfiguration addJarFile(@Nonnull final String fileName) {
        Validate.isTrue(Files.exists(Paths.get(fileName)), "File '%s' was not found!", fileName);
        walaScopeFileContent.add("Application,Java,jarFile," + fileName);
        return this;
    }

    public ApplicationConfiguration addJarFiles(@Nonnull final List<String> fileNames) {
        for (String fileName : fileNames) {
            addJarFile(fileName);
        }
        return this;
    }

    public ApplicationConfiguration addGraphOutputDir(@Nonnull final String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    @Nonnull
    private static String createTempFile(@Nonnull final String name,
                                         @Nonnull final List<String> content) {
        try {
            final Path tempFile = Files.createTempFile(name, ".txt").toAbsolutePath();
            Files.write(tempFile, content);
            log.trace("Created temporary file {} with content {}", tempFile, new CollectionToString<>(content));
            return tempFile.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create temporary file", e);
        }
    }
}
