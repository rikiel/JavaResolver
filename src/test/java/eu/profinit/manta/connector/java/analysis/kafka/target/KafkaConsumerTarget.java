package eu.profinit.manta.connector.java.analysis.kafka.target;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import eu.profinit.manta.connector.java.analysis.TestWrapper;

public class KafkaConsumerTarget extends AbstractKafkaTarget {
    public static void runKafkaConsumer() {
        try (final Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(getKafkaProperties())) {
            consumer.subscribe(Collections.singletonList(CONSUMER_TOPIC));

            consumer.poll(Duration.ZERO);
        }
    }

    @SuppressWarnings("deprecation")
    public static void runKafkaConsumer1() {
        try (final Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(getKafkaProperties())) {
            consumer.subscribe(Collections.singletonList(CONSUMER_TOPIC));

            consumer.poll(1);
        }
    }

    public static void runKafkaConsumerAssign() {
        try (final Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(getKafkaProperties())) {
            consumer.assign(Collections.singletonList(new TopicPartition(CONSUMER_TOPIC, 1)));

            consumer.poll(Duration.ZERO);
        }
    }
}
