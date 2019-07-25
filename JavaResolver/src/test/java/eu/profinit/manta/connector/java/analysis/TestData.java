package eu.profinit.manta.connector.java.analysis;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.testng.Assert;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.IGraph;

public class TestData {
    @Nonnull
    private final String runnerMethodName;
    @Nonnull
    private final Consumer<IGraph> graphValidator;

    public TestData(@Nonnull final String runnerMethodName, @Nonnull final Consumer<IGraph> graphValidator) {
        Validate.notNullAll(runnerMethodName, graphValidator);

        this.runnerMethodName = runnerMethodName;
        this.graphValidator = graphValidator;
    }

    public TestData(@Nonnull final String runnerMethodName) {
        this(runnerMethodName, iGraph -> Assert.fail("Graph validations are missing!!"));
    }

    @Nonnull
    public String getRunnerMethodName() {
        return runnerMethodName;
    }

    public void validateResultGraph(@Nonnull final IGraph iGraph) {
        Validate.notNull(iGraph);
        graphValidator.accept(iGraph);
    }

    @Override
    public String toString() {
        return runnerMethodName;
    }
}
