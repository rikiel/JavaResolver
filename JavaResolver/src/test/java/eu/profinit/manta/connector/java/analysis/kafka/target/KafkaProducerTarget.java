package eu.profinit.manta.connector.java.analysis.kafka.target;

import java.util.ArrayList;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import eu.profinit.manta.connector.java.analysis.TestWrapper;

public class KafkaProducerTarget extends AbstractKafkaTarget {
    public static void runKafkaProducer() {
        try (Producer<String, String> producer = new KafkaProducer<>(getKafkaProperties())) {
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, "Message1"));
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, "Key", "Message2"));
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, 1, "Key", "Message3"));
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, 1, "Key", "Message4", new ArrayList<>()));
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, 1, 1L, "Key", "Message5"));
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, 1, 1L, "Key", "Message6", new ArrayList<>()));
        }
    }

    public static void runKafkaProducerWithPropertiesFromFile() {
        try (Producer<String, String> producer = new KafkaProducer<>(getKafkaPropertiesFromFile())) {
            producer.send(new ProducerRecord<>(PRODUCER_TOPIC, "Message1"));
        }
    }
}
