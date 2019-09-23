package eu.profinit.manta.connector.java.analysis.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils.PrimitiveType;
import eu.profinit.manta.connector.java.resolver.TaskExecutor;

/**
 * Cache for storing precomputed data.
 */
public class ClassMethodCache {
    @Nonnull
    private final IClassHierarchy iClassHierarchy;
    @Nonnull
    private final Data data;
    @Nonnull
    private final List<JarFile> jarFiles;

    public ClassMethodCache(@Nonnull final IClassHierarchy iClassHierarchy) {
        this.iClassHierarchy = iClassHierarchy;

        this.data = TaskExecutor.compute("Construct class method cache", () -> new Data(iClassHierarchy));

        this.jarFiles = ImmutableList.copyOf(iClassHierarchy.getScope().getModules(ClassLoaderReference.Application).stream()
                .map(module -> module instanceof JarFileModule ? ((JarFileModule) module) : null)
                .filter(Objects::nonNull)
                .map(JarFileModule::getJarFile)
                .collect(Collectors.toList()));
    }

    /**
     * @param signature Signatura metody
     * @return Vrati metodu so signaturou {@code signature}
     */
    @Nonnull
    public IMethod findMethod(@Nonnull final String signature) {
        final IMethod result = data.nameToMethod.get(signature);
        Validate.notNull(result, "Method %s was not found in cache!", signature);
        return result;
    }

    @Nonnull
    public IClassHierarchy getClassHierarchy() {
        return iClassHierarchy;
    }

    @Nonnull
    public ImmutableCollection<IClass> getAllClasses() {
        return data.nameToClass.values();
    }

    @Nonnull
    public ImmutableCollection<IMethod> getAllMethods() {
        return data.nameToMethod.values();
    }

    @Nonnull
    public IClass findClass(@Nonnull final ClassWrapper clazz) {
        final IClass result = data.nameToClass.get(clazz.getJavaClassName());
        Validate.notNull(result, "Class %s was not found in cache!", clazz);
        return result;
    }

    @Nonnull
    public List<JarFile> getJarFiles() {
        return jarFiles;
    }

    private static class Data {
        private final ImmutableMap<String, IClass> nameToClass;
        private final ImmutableMap<String, IMethod> nameToMethod;

        private Data(@Nonnull final IClassHierarchy iClassHierarchy) {
            final Map<String, IClass> nameToClass = Maps.newHashMap();
            final Map<String, IMethod> nameToMethod = Maps.newHashMap();
            for (IClass iClass : Sets.newHashSet(iClassHierarchy)) {
                final String className = new ClassWrapperImpl(iClass.getName()).getJavaClassName();
                nameToClass.putIfAbsent(className, iClass);

                for (IClass superClass : WalaUtils.getAllSuperClasses(iClass)) {
                    for (IMethod iMethod : superClass.getDeclaredMethods()) {
                        final String signature = iMethod.getSignature();
                        final String signatureWithoutClassName = signature.substring(signature.lastIndexOf("."));
                        final String methodName = className + signatureWithoutClassName;
                        nameToMethod.putIfAbsent(methodName, iMethod);
                    }
                }
            }
            for (PrimitiveType value : PrimitiveType.values()) {
                final IClass objectClass = nameToClass.get(value.getObjectClass().getJavaClassName());
                Validate.notNull(objectClass, "Class %s was not found in cache!", value.getObjectClass());
                nameToClass.put(value.getPrimitiveClass().getJavaClassName(), objectClass);
            }

            this.nameToClass = ImmutableMap.copyOf(nameToClass);
            this.nameToMethod = ImmutableMap.copyOf(nameToMethod);
        }
    }
}
