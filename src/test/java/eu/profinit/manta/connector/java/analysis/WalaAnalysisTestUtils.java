package eu.profinit.manta.connector.java.analysis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections.iterators.SingletonIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.ClassFileModule;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.resolver.TaskExecutor;

public class WalaAnalysisTestUtils {
    private static final Logger log = LoggerFactory.getLogger(WalaAnalysisTestUtils.class);
    public static final List<String> DEFAULT_STDLIB_SCOPE = Lists.newArrayList(
            "Primordial,Java,stdlib,none");
    public static final List<String> TESTS_SCOPE = Lists.newArrayList(
            /* Tests */
            "./target/libs/java-resolver-1.0-SNAPSHOT-tests.jar"
    );
    public static final List<String> MY_BATIS_SCOPE = Lists.newArrayList(
            "./target/libs/mybatis-3.4.6.jar");
    public static final List<String> JDBC_TEMPLATE_SCOPE = Lists.newArrayList(
            "./target/libs/spring-jdbc-4.3.14.RELEASE.jar",
            "./target/libs/spring-core-4.3.14.RELEASE.jar",
            "./target/libs/spring-tx-4.3.14.RELEASE.jar",
            "./target/libs/commons-logging-1.1.jar"
    );
    public static final List<String> KAFKA_SCOPE = Lists.newArrayList(
            "./target/libs/kafka-clients-2.1.0.jar");
    public static final List<String> DATASOURCE_SCOPE = Lists.newArrayList(
            /* ApacheCommons */
            "./target/libs/commons-dbcp2-2.5.0.jar",
            /* TeraData */
            "./target/libs/terajdbc4-15.00.00.35.jar",
            /* Oracle */
            "./target/libs/ojdbc7-12.1.0.2.20181101.jar",
            /* MsSql */
            "./target/libs/mssql-jdbc-7.2.0.jre8.jar",
            /* Postgres */
            "./target/libs/postgresql-42.2.5.jre7.jar",
            /* Db2 */
            "./target/libs/jcc-11.1.4.4.jar",
            /* Embedded */
            "./target/libs/spring-jdbc-4.3.14.RELEASE.jar");

    private static final ClassLoader CLASS_LOADER = WalaAnalysisTestUtils.class.getClassLoader();

    @Nonnull
    public static IClassHierarchy constructClassHierarchy(@Nullable final List<Class<?>> classesToAnalyze, @Nullable final List<String> jarFiles) {
        try {
            final List<Class<?>> classesList = classesToAnalyze == null ? ImmutableList.of() : classesToAnalyze;
            final List<String> jarFileList = jarFiles == null ? ImmutableList.of() : jarFiles;

            final AnalysisScope analysisScope = TaskExecutor.compute("Construct analysis scope",
                    () -> getAnalysisScope(classesList, jarFileList));
            return TaskExecutor.compute("Construct class hierarchy",
                    () -> ClassHierarchyFactory.make(analysisScope));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static IClass constructClassHierarchyAndGetApplicationClass(@Nonnull final Class<?> testedClass, @Nonnull final List<Class<?>> classesToAnalyze) {
        final IClassHierarchy classHierarchy = constructClassHierarchy(classesToAnalyze, null);
        return Lists.newArrayList(classHierarchy.getLoader(ClassLoaderReference.Application).iterateAllClasses()).stream()
                .filter(iClass -> WalaUtils.isSupertype(iClass, new ClassWrapperImpl(testedClass)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Class was not found in class hierarchy: " + testedClass));
    }

    @Nonnull
    private static AnalysisScope getAnalysisScope(@Nonnull final List<Class<?>> classesToAnalyze, @Nonnull final List<String> jarFiles) throws IOException {
        final AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

        addScope(scope, DEFAULT_STDLIB_SCOPE);
        addJarsToScope(scope, jarFiles);
        if (classesToAnalyze.isEmpty()) {
            addJarsToScope(scope, TESTS_SCOPE);
        } else {
            for (Class<?> scopeClass : classesToAnalyze) {
                boolean added = false;
                final String className = scopeClass.getName().replaceAll("\\.", "/") + ".class";
                for (String compiledClassesDir : Lists.newArrayList("classes", "test-classes")) {
                    final File dir = new File(new File(new File("./"), "target"), compiledClassesDir);
                    if (!dir.exists()) {
                        continue;
                    }
                    final File classFile = new File(dir, className);
                    if (!classFile.exists()) {
                        continue;
                    }
                    scope.addToScope(ClassLoaderReference.Application, new ClassModule(classFile));
                    log.debug("Adding class {} to scope", scopeClass.getSimpleName());
                    added = true;
                }
                for (String jar : jarFiles) {
                    final JarFile jarFile = new JarFile(jar);
                    final ZipEntry entry = jarFile.getEntry(className);
                    if (entry != null) {
                        scope.addToScope(ClassLoaderReference.Application, new ClassModule(jarFile, entry, className));
                        log.debug("Adding class {} to scope from jar {}", scopeClass.getSimpleName(), jarFile.getName());
                        added = true;
                    }
                }
                Validate.isTrue(added, "Can not add class to scope: %s", scopeClass);
            }
        }

        return scope;
    }

    private static void addScope(@Nonnull final AnalysisScope scope, @Nonnull final List<String> scopeLines) throws IOException {
        for (String scopeLine : scopeLines) {
            AnalysisScopeReader.processScopeDefLine(scope, CLASS_LOADER, scopeLine);
        }
    }

    private static void addJarsToScope(@Nonnull final AnalysisScope scope, @Nonnull final List<String> jars) throws IOException {
        addScope(scope, jars.stream().map(jar -> "Application,Java,jarFile," + jar).collect(Collectors.toList()));
    }

    /**
     * For including single class file
     */
    private static final class ClassModule implements Module {
        private final ModuleEntry entry;

        private ClassModule(@Nonnull final File classFile) {
            Validate.notNull(classFile);
            try {
                this.entry = new ClassFileModule(classFile, this);
            } catch (InvalidClassFileException e) {
                throw new IllegalStateException("Failed to create module for file.", e);
            }
        }

        private ClassModule(@Nonnull final JarFile jarFile,
                            @Nonnull final ZipEntry entry,
                            @Nonnull final String className) {
            Validate.notNullAll(jarFile, entry, className);
            this.entry = new ClassInJarModuleEntry(jarFile, entry, className);
        }

        @Override
        public Iterator<? extends ModuleEntry> getEntries() {
            return new SingletonIterator(entry);
        }

        private final class ClassInJarModuleEntry implements ModuleEntry {
            private final JarFile jarFile;
            private final ZipEntry entry;
            private final String className;

            public ClassInJarModuleEntry(JarFile jarFile, ZipEntry entry, String className) {
                this.jarFile = jarFile;
                this.entry = entry;
                this.className = className;
            }

            @Override
            public String getName() {
                return jarFile.getName();
            }

            @Override
            public boolean isClassFile() {
                return true;
            }

            @Override
            public boolean isSourceFile() {
                return false;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return jarFile.getInputStream(entry);
                } catch (IOException e) {
                    return Validate.fail(e);
                }
            }

            @Override
            public boolean isModuleFile() {
                return false;
            }

            @Override
            public Module asModule() {
                return Validate.fail("Not supported");
            }

            @Override
            public String getClassName() {
                return className;
            }

            @Override
            public Module getContainer() {
                return ClassModule.this;
            }
        }
    }
}
