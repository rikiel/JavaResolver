package eu.profinit.manta.connector.java.analysis;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.analysis.utils.Validate;

public class TargetRunner implements Runnable {
    private final String methodName;
    private final Runnable runnable;

    public TargetRunner(@Nonnull final String methodName, @Nonnull final Runnable runnable) {
        Validate.notNullAll(methodName, runnable);
        this.methodName = methodName;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

    @Override
    public String toString() {
        return methodName;
    }
}
