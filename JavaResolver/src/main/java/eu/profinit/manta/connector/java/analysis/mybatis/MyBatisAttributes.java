package eu.profinit.manta.connector.java.analysis.mybatis;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

public enum MyBatisAttributes implements IAttributeName {
    ENVIRONMENT_NAME("MYBATIS_ENVIRONMENT_NAME"),
    DATASOURCE_MAP_KEY("DATASOURCE_MAP_KEY"),
    MAPPER_METHOD("MYBATIS_MAPPER_METHOD"),
    MAPPER_CLASS("MYBATIS_MAPPER_CLASS"),
    ;

    private final String attributeName;

    MyBatisAttributes(@Nonnull final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    @Nonnull
    public String getAttributeName() {
        return attributeName;
    }
}
