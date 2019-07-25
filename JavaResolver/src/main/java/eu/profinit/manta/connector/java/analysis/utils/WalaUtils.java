package eu.profinit.manta.connector.java.analysis.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeCT.AnnotationsReader.ConstantElementValue;
import com.ibm.wala.shrikeCT.AnnotationsReader.ElementValue;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.common.ClassMethodCache;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;
import eu.profinit.manta.connector.java.analysis.common.ClassWrapperImpl;

@SuppressWarnings("WeakerAccess")
public class WalaUtils {
    private static final ClassWrapperImpl OBJECT_CLASS = new ClassWrapperImpl(Object.class);
    private static final ClassWrapperImpl CONSTRUCTOR_RESULT_CLASS = new ClassWrapperImpl(void.class);
    private static final String CONSTRUCTOR_METHOD_NAME = "<init>";

    private WalaUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param annotation    Annotation
     * @param expectedClass Requested annotation type
     * @param parameterName Name of annotation parameter
     * @return {@code null}, if annotation is not present or parameter does not exist. Otherwise the value of parameter.
     */
    @Nullable
    public static String getParameterValue(@Nonnull final Annotation annotation,
                                           @Nonnull final ClassWrapper expectedClass,
                                           @Nonnull final String parameterName) {
        if (Objects.equals(expectedClass.getWalaTypeName(), annotation.getType().getName())) {
            final ElementValue value = annotation.getNamedArguments().get(parameterName);
            if (value != null) {
                Validate.isTrue(value instanceof ConstantElementValue, "Found wrong type of annotation");
                return value.toString();
            }
        }
        return null;
    }

    /**
     * @param iClass Class
     * @return Returns list of method declared in {@code iClass} or its ancestors. Methods of {@link Object} are not included.
     */
    @Nonnull
    public static List<IMethod> getAllInterfaceMethods(@Nonnull final IClass iClass) {
        return iClass.getAllMethods().stream()
                .filter(iMethod -> !Objects.equals(iMethod.getDeclaringClass().getName(), OBJECT_CLASS.getWalaTypeName()))
                .collect(Collectors.toList());
    }

    /**
     * @param classMethodCache Cache
     * @param clazz            Parent class
     * @return Returns list of all subclasses of {@code clazz}.
     */
    @Nonnull
    public static List<IClass> getAllSubClasses(@Nonnull final ClassMethodCache classMethodCache,
                                                @Nonnull final ClassWrapper clazz) {
        return classMethodCache.getAllClasses().stream()
                .filter(iClass -> isSupertype(iClass, clazz))
                .collect(Collectors.toList());
    }

    /**
     * @param classMethodCache Cache
     * @param classes          Classes for which methods we want to find
     * @return Returns list of methods declared/overriden in classes or their subclasses.
     */
    @Nonnull
    public static List<IMethod> getDeclaredMethodsForAllSubclasses(@Nonnull final ClassMethodCache classMethodCache,
                                                                   @Nonnull final ClassWrapper... classes) {
        return Arrays.stream(classes)
                .flatMap(clazz -> getAllSubClasses(classMethodCache, clazz).stream())
                .map(IClass::getDeclaredMethods)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * @param iClass    Class
     * @param supertype Supertype
     * @return Returns whether {@code supertype} is supertype (class/interface) of class {@code iClass}
     */
    public static boolean isSupertype(@Nonnull final IClass iClass, @Nonnull final ClassWrapper supertype) {
        final ClassWrapper objectSupertype = PrimitiveType.toPrimitiveObjectOrOriginal(supertype);
        return getAllSuperClasses(iClass).stream()
                .anyMatch(superClass -> isExactClassType(superClass, objectSupertype));
    }

    /**
     * @param iClass Class
     * @param expectedClass Expected class
     * @return Returns whether {@code iClass} is a class with type {@code expectedClass}
     */
    public static boolean isExactClassType(@Nonnull final IClass iClass, @Nonnull final ClassWrapper expectedClass) {
        return Objects.equals(iClass.getName(), expectedClass.getWalaTypeName());
    }

    /**
     * @param clazz         Class
     * @param expectedClass Expected class
     * @return Returns whether {@code iClass} is a class with type {@code expectedClass}
     */
    public static boolean isExactClassType(@Nonnull final ClassWrapper clazz, @Nonnull final ClassWrapper expectedClass) {
        return ClassWrapper.isSame(clazz, expectedClass);
    }

    /**
     * @param iMethod Method
     * @param name    Expected method name
     * @return Returns whether name of {@code iMethod} is the same as in {@code name}
     */
    public static boolean isNamed(@Nonnull final IMethod iMethod, String name) {
        return Objects.equals(iMethod.getName().toString(), name);
    }

    /**
     * @param iMethod Method
     * @return Returns list of method arguments. It ignores {@code this} argument for nonstatic classes.
     */
    @Nonnull
    public static List<ClassWrapper> getArguments(@Nonnull final IMethod iMethod) {
        return IntStream.range(iMethod.isStatic() ? 0 : 1, iMethod.getNumberOfParameters())
                .mapToObj(iMethod::getParameterType)
                .map(TypeReference::getName)
                .map(ClassWrapperImpl::new)
                .map(PrimitiveType::toPrimitiveObjectOrOriginal)
                .collect(Collectors.toList());
    }

    /**
     * @param iClass Class
     * @return Returns all instance fields of {@code iClass}
     */
    @Nonnull
    public static List<IField> getAllInstanceFields(@Nonnull final IClass iClass) {
        return Lists.newArrayList(iClass.getAllInstanceFields());
    }

    /**
     * @param classMethodCache Cache
     * @param callerClass      Caller class
     * @param argumentClasses  Expected argument types
     * @return Returns constructor in class {@code callerClass} with arguments as in {@code argumentClasses}
     * @see WalaUtils#findMethod(ClassMethodCache, IClass, String, ClassWrapper, ClassWrapper...)
     */
    @Nonnull
    public static IMethod findConstructorMethod(@Nonnull final ClassMethodCache classMethodCache,
                                                @Nonnull final ClassWrapper callerClass,
                                                @Nonnull final ClassWrapper... argumentClasses) {
        return findMethod(classMethodCache,
                classMethodCache.findClass(callerClass),
                CONSTRUCTOR_METHOD_NAME,
                CONSTRUCTOR_RESULT_CLASS,
                argumentClasses);
    }

    /**
     * @param classMethodCache Cache
     * @param callerClass      Caller class
     * @param methodName       Expected method name
     * @param resultClass      Expected return type of a method
     * @param argumentClasses  Expected argument types
     * @return Returns method in class {@code callerClass} where:
     * <ul>
     *     <li>Name of method is same as in {@code methodName}</li>
     *     <li>Return type is as in {@code resultClass}</li>
     *     <li>Arguments are as in {@code argumentClasses}</li>
     * </ul>
     */
    @Nonnull
    public static IMethod findMethod(@Nonnull final ClassMethodCache classMethodCache,
                                     @Nonnull final ClassWrapper callerClass,
                                     @Nonnull final String methodName,
                                     @Nonnull final ClassWrapper resultClass,
                                     @Nonnull final ClassWrapper... argumentClasses) {
        return findMethod(classMethodCache,
                classMethodCache.findClass(callerClass),
                methodName,
                resultClass,
                argumentClasses);
    }

    /**
     * @param classMethodCache Cache
     * @param callerClass      Caller class
     * @param methodName       Expected method name
     * @param resultClass      Expected return type of a method
     * @param argumentClasses  Expected argument types
     * @return Returns method in class {@code callerClass} where:
     * <ul>
     *     <li>Name of method is same as in {@code methodName}</li>
     *     <li>Return type is as in {@code resultClass}</li>
     *     <li>Arguments are as in {@code argumentClasses}</li>
     * </ul>
     */
    @Nonnull
    public static IMethod findMethod(@Nonnull final ClassMethodCache classMethodCache,
                                     @Nonnull final IClass callerClass,
                                     @Nonnull final String methodName,
                                     @Nonnull final ClassWrapper resultClass,
                                     @Nonnull final ClassWrapper... argumentClasses) {
        Validate.notNullAll(classMethodCache, callerClass, methodName, argumentClasses, resultClass);

        final ClassWrapper resultObjectClass = PrimitiveType.toPrimitiveObjectOrOriginal(resultClass);
        final List<ClassWrapper> argumentClassesList = Arrays.asList(argumentClasses);
        final List<IMethod> matchingMethods = callerClass.getAllMethods().stream()
                // matches method name
                .filter(iMethod -> isNamed(iMethod, methodName))
                // matches return type
                .filter((IMethod iMethod) -> {
                    final ClassWrapperImpl returnClass = new ClassWrapperImpl(iMethod.getReturnType().getName());
                    final ClassWrapper returnObjectClass = PrimitiveType.toPrimitiveObjectOrOriginal(returnClass);
                    return ClassWrapper.isSame(resultObjectClass, returnObjectClass);
                })
                // matches all argument types
                .filter(iMethod -> isMatchingArguments(iMethod, argumentClasses))
                .collect(Collectors.toList());

        Validate.validState(matchingMethods.size() == 1,
                "Failed to found one matching method for pattern: {callerClass=%s; methodName=%s, argumentClasses=%s, resultClass=%s}! Found %s",
                callerClass,
                methodName,
                argumentClassesList,
                resultClass,
                matchingMethods);
        return matchingMethods.get(0);
    }

    /**
     * @param iClass Class
     * @return Returns list of super classes/implemented interfaces of {@code iClass}. The {@code iClass} is also included.
     */
    @Nonnull
    public static List<IClass> getAllSuperClasses(@Nonnull final IClass iClass) {
        final List<IClass> result = Lists.newArrayList();
        for (IClass superClass = iClass; superClass != null; superClass = superClass.getSuperclass()) {
            result.add(superClass);
        }
        result.addAll(iClass.getAllImplementedInterfaces());
        return result;
    }

    /**
     * @param iMethod               Method
     * @param expectedArgumentTypes Expected argument types
     * @return Returns whether method arguments are the same as in {@code expectedArgumentTypes}
     */
    public static boolean isMatchingArguments(@Nonnull final IMethod iMethod,
                                              @Nonnull final ClassWrapper... expectedArgumentTypes) {
        Validate.notNullAll(iMethod, expectedArgumentTypes);

        final List<ClassWrapper> expectedArgumentTypesList = Stream.of(expectedArgumentTypes)
                .map(PrimitiveType::toPrimitiveObjectOrOriginal)
                .collect(Collectors.toList());
        final List<ClassWrapper> actualArguments = getArguments(iMethod);
        if (actualArguments.size() != expectedArgumentTypesList.size()) {
            return false;
        }

        for (int i = 0; i < actualArguments.size(); ++i) {
            if (!isExactClassType(actualArguments.get(i), expectedArgumentTypesList.get(i))) {
                return false;
            }
        }
        return true;
    }

    public enum PrimitiveType {
        VOID(void.class, Void.class),
        INTEGER(int.class, Integer.class),
        LONG(long.class, Long.class),
        SHORT(short.class, Short.class),
        BYTE(byte.class, Byte.class),
        CHAR(char.class, Character.class),
        DOUBLE(double.class, Double.class),
        FLOAT(float.class, Float.class),
        BOOLEAN(boolean.class, Boolean.class),
        ;

        private static final Map<String, PrimitiveType> nameToPrimitive;
        private final ClassWrapper primitiveClass;
        private final ClassWrapper objectClass;

        static {
            final ImmutableMap.Builder<String, PrimitiveType> builder = ImmutableMap.builder();
            for (PrimitiveType value : values()) {
                builder.put(value.getObjectClass().getJavaClassName(), value);
                builder.put(value.getPrimitiveClass().getJavaClassName(), value);
            }
            nameToPrimitive = builder.build();
        }

        /**
         * @param primitiveClass Primitive version of a class
         * @param objectClass    Object version of a class
         * @see StringStuff Field primitiveClassNames in StringStuff class
         */
        PrimitiveType(@Nonnull final Class<?> primitiveClass, @Nonnull final Class<?> objectClass) {
            this.primitiveClass = new ClassWrapperImpl(primitiveClass);
            this.objectClass = new ClassWrapperImpl(objectClass);
        }

        @Nonnull
        public static ClassWrapper toPrimitiveObjectOrOriginal(@Nonnull final ClassWrapper clazz) {
            final PrimitiveType primitiveType = nameToPrimitive.get(clazz.getJavaClassName());
            return primitiveType == null
                    ? clazz
                    : primitiveType.getObjectClass();
        }

        @Nonnull
        public ClassWrapper getPrimitiveClass() {
            return primitiveClass;
        }

        @Nonnull
        public ClassWrapper getObjectClass() {
            return objectClass;
        }
    }
}
