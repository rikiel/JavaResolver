package eu.profinit.manta.connector.java.analysis.kafka.target;

import java.time.Duration;
import java.util.Collections;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaMixedTarget extends AbstractKafkaTarget {
    public static void runKafkaProducerAndConsumer() {
        try (final Consumer<String, String> consumer = new KafkaConsumer<>(getKafkaProperties())) {
            consumer.subscribe(Collections.singletonList(CONSUMER_TOPIC));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            try (Producer<Object, Object> producer = new KafkaProducer<>(getKafkaProperties())) {
                for (ConsumerRecord<String, String> record : records) {
                    producer.send(new ProducerRecord<>(PRODUCER_TOPIC, record));
                }
            }
        }
    }
}
