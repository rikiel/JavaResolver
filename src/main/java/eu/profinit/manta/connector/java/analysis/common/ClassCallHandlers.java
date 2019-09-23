package eu.profinit.manta.connector.java.analysis.common;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.ibm.wala.classLoader.IMethod;

import eu.profinit.manta.connector.java.analysis.common.plugin.MethodCallDescriptionImpl;
import eu.profinit.manta.connector.java.analysis.common.plugin.MethodEffectsDescriptionBuilder;
import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.analysis.utils.WalaUtils;

public class ClassCallHandlers extends CallHandlers {
    @Nonnull
    private final ClassWrapper handlingClass;

    public ClassCallHandlers(@Nonnull final Object callerObject,
                             @Nonnull final ClassWrapper handlingClass,
                             @Nonnull final CallHandler... handlers) {
        super(callerObject, handlers);
        Validate.notNull(handlingClass);
        this.handlingClass = handlingClass;
    }

    @Override
    public boolean canHandle(@Nonnull IMethod iMethod) {
        return WalaUtils.isSupertype(iMethod.getDeclaringClass(), handlingClass)
               && super.canHandle(iMethod);
    }

    @Override
    protected void logResult(@Nonnull MethodCallDescriptionImpl methodCallDescription,
                             @Nonnull MethodEffectsDescriptionBuilder result,
                             @Nonnull Handler handler) {
        // do nothing, as message will be logged from parent handler
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("handlingClass", handlingClass)
                .toString();
    }
}
