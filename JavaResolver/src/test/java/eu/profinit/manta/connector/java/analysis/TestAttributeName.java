package eu.profinit.manta.connector.java.analysis;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

public class TestAttributeName implements IAttributeName {
    private final String attributeName;

    public TestAttributeName(@Nonnull final String attributeName) {
        Validate.notNull(attributeName);
        this.attributeName = attributeName;
    }

    @Nonnull
    @Override
    public String getAttributeName() {
        return attributeName;
    }
}
