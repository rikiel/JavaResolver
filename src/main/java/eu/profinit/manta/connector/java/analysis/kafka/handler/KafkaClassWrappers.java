package eu.profinit.manta.connector.java.analysis.kafka.handler;

import javax.annotation.Nonnull;

import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.strings.StringStuff;

import eu.profinit.manta.connector.java.analysis.common.ClassWrapper;

public enum KafkaClassWrappers implements ClassWrapper {
    CONSUMER("org.apache.kafka.clients.consumer.Consumer"),
    CONSUMER_REBALANCE_LISTENER("org.apache.kafka.clients.consumer.ConsumerRebalanceListener"),
    CONSUMER_CONFIG("org.apache.kafka.clients.consumer.ConsumerConfig"),
    CONSUMER_RECORD("org.apache.kafka.clients.consumer.ConsumerRecord"),
    CONSUMER_RECORDS("org.apache.kafka.clients.consumer.ConsumerRecords"),
    HEADERS("org.apache.kafka.common.header.Headers"),
    TIMESTAMP_TYPE("org.apache.kafka.common.record.TimestampType"),
    TOPIC_PARTITION("org.apache.kafka.common.TopicPartition"),
    CALLBACK("org.apache.kafka.clients.producer.Callback"),
    PRODUCER("org.apache.kafka.clients.producer.Producer"),
    PRODUCER_CONFIG("org.apache.kafka.clients.producer.ProducerConfig"),
    PRODUCER_RECORD("org.apache.kafka.clients.producer.ProducerRecord"),
    RECORD_METADATA("org.apache.kafka.clients.producer.RecordMetadata"),
    ;

    private final String javaClassName;
    private final TypeName walaTypeName;

    KafkaClassWrappers(@Nonnull final String javaClassName) {
        this.javaClassName = javaClassName;
        this.walaTypeName = TypeName.findOrCreate(StringStuff.deployment2CanonicalTypeString(javaClassName));
    }

    @Nonnull
    @Override
    public String getJavaClassName() {
        return javaClassName;
    }

    @Nonnull
    @Override
    public TypeName getWalaTypeName() {
        return walaTypeName;
    }
}
