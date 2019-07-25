package eu.profinit.manta.connector.java.analysis.datasource;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.analysis.utils.Validate;
import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

public enum DataSourceAttributes implements IAttributeName {
    DATABASE_NAME("DATABASE_NAME"),
    ;

    private final String attributeName;

    DataSourceAttributes(@Nonnull final String attributeName) {
        Validate.notNull(attributeName);
        this.attributeName = attributeName;
    }

    @Nonnull
    @Override
    public String getAttributeName() {
        return attributeName;
    }
}
