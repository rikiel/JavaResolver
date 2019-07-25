package eu.profinit.manta.connector.java.analysis.kafka;

import javax.annotation.Nonnull;

import eu.profinit.manta.connector.java.model.flowgraph.IAttributeName;

public enum KafkaAttributes implements IAttributeName {
    SERVER("KAFKA_SERVER"),
    PARTITION("KAFKA_PARTITION"),
    KEY("KAFKA_KEY"),
    VALUE("KAFKA_VALUE"),
    TOPIC("KAFKA_TOPIC_NAME"),
    BACKEND_CALL("KAFKA_BACKEND_CALL"),
    ;

    private final String attributeName;

    KafkaAttributes(@Nonnull final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    @Nonnull
    public String getAttributeName() {
        return attributeName;
    }
}
